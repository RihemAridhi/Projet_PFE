#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Firebase_ESP_Client.h>
#include <ESP32Servo.h>

LiquidCrystal_I2C lcd(0x27, 16, 2);
Servo myServo;

#define SERVO_PIN 13
#define LED_VERTE 25
#define LED_ROUGE 26
#define BUZZER_PIN 14



const char* ssid = "";
const char* password = "";
String lastTimestamp = "";
int current_spots = 0;


FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;



void setup() {
  Serial.begin(115200);
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Initialisation...");

  myServo.attach(SERVO_PIN);
  pinMode(LED_VERTE, OUTPUT);
  pinMode(LED_ROUGE, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    lcd.print(".");
  }

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("WiFi Connecte");

  // Firebase config
  config.api_key = "";
  auth.user.email = "";
  auth.user.password = "";
  config.database_url = "";
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  delay(2000);
  lcd.clear();
}

void loop() {
  if (Firebase.RTDB.getJSON(&fbdo, "parking/response")) {
    FirebaseJson& json = fbdo.jsonObject();
    FirebaseJsonData result;
    String plate = "", timestamp = "", direction = "";
    bool authorized = false;
    int spots = 0;
    bool handled = true;
    String employeeName = "";
    if (json.get(result, "employee_name")) employeeName = result.to<String>();
    if (json.get(result, "plate")) plate = result.to<String>();
    if (json.get(result, "timestamp")) timestamp = result.to<String>();
    if (json.get(result, "authorized")) authorized = result.to<bool>();
    if (json.get(result, "available_spots")) {
      spots = result.to<int>();
      current_spots = spots;  // mémoriser pour affichage permanent
    }
    if (json.get(result, "handled")) handled = result.to<bool>();
    if (json.get(result, "direction")) direction = result.to<String>();

    // Si déjà traité ou même timestamp, on affiche juste l'écran principal
    if (timestamp == lastTimestamp || handled == true) {
      lcd.setCursor(0, 0);
      lcd.print("Bienvenue au park");
      lcd.setCursor(0, 1);
      lcd.print("Places dispo: ");
      lcd.print(current_spots);
      //lcd.print("     "); // effacer caractères restants
      delay(3000);
      return;
    }

    // Traitement seulement si nouveau et non traité
    lastTimestamp = timestamp;

    if (direction == "entry") {
      Serial.println("Plaque à l'entrée : " + plate);
      lcd.clear();
      lcd.setCursor(0, 0);
      if (employeeName != "") {
       lcd.print(employeeName );
      }
      lcd.print(" placez la  voiture");
      lcd.setCursor(0, 1);

      if (authorized && spots > 0) {
        lcd.print("Access: autorisée bon journéé  ");
        digitalWrite(LED_VERTE, HIGH);
        digitalWrite(LED_ROUGE, LOW);
        myServo.write(90);
        delay(5000);
        myServo.write(0);
        digitalWrite(LED_VERTE, LOW);
      } else if (!authorized) {
        lcd.print("Access: REFUSE");
        digitalWrite(LED_ROUGE, HIGH);
        digitalWrite(BUZZER_PIN, HIGH);
        delay(1500);
        digitalWrite(BUZZER_PIN, LOW);
        digitalWrite(LED_ROUGE, LOW);
      } else if (authorized && spots == 0) {
        lcd.print("Parking Plein!");
        digitalWrite(BUZZER_PIN, HIGH);
        delay(1500);
        digitalWrite(BUZZER_PIN, LOW);
      }
    }

    // Pour une sortie, on ne change pas l’écran (pas de lcd.clear)
    else if (direction == "exit") {
      Serial.println("Sortie détectée : " + plate);
      myServo.write(90);
      delay(5000);
      myServo.write(0);
      // Pas de message LCD
    }

    // Marquer comme traité
    Firebase.RTDB.setBool(&fbdo, "parking/response/handled", true);
  }

  delay(2000);
}
