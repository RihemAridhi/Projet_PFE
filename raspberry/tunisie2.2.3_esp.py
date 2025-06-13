# --- Dépendances ---final code
from ultralytics import YOLO
import cv2
import cvzone
import os
import numpy as np
from datetime import datetime, date
import firebase_admin
from firebase_admin import credentials, db
import re
import easyocr 

# --- Répertoire de travail ---
os.chdir('C:/Users/DELL/anaconda3/envs/computer/matricule')  # adapte ce chemin si besoin

# --- Initialisation Firebase ---
cred = credentials.Certificate("smartparkingapp-48ba1-firebase-adminsdk-fbsvc-a8e2268843.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://smartparkingapp-48ba1-default-rtdb.firebaseio.com/'
})

# --- Charger les plaques valides depuis Firebase ---
def load_valid_plates():
    valid_ref = db.reference('employees')
    data = valid_ref.get()
    plate_id_map = {}
    name_map = {}
    if data:
        for employee in data.values():
            plate_number = employee.get('plate_number')
            emp_id = employee.get('id')
            name = employee.get('employee_name')  # ou 'full_name', selon ta base Firebase
            if plate_number and emp_id:
                plate_id_map[plate_number] = emp_id
                name_map[plate_number] = name
    return plate_id_map, name_map


# Correction automatique OCR 
def correct_ocr_errors(plate):
    plate = plate.replace(" ", "").upper()
    plate = plate.replace("تونن", "تونس")
    plate = plate.replace("ثونس", "تونس")
    plate = plate.replace("توس", "تونس")
    plate = plate.replace("تون", "تونس")
    plate = re.sub(r"(تونس)+[س]*", "تونس", plate)
    
    # Supprime tout sauf les chiffres et "تونس"
    plate = re.sub(r'[^0-9تونس]', '', plate)
    
    # Recherche un motif : 3 chiffres + تونس + 3 ou 4 chiffres
    match = re.search(r'(\d{3,4})تونس(\d{1,3})', plate)
    if match:
        return f"{match.group(1)}تونس{match.group(2)}"
    
    # Si "تونس" n'est pas détecté, essaie de reconstituer à partir de chiffres
    digits = re.findall(r'\d+', plate)
    if len(digits) >= 2 and 1 <= len(digits[0]) <= 3 and 3 <= len(digits[1]) <= 4:
        return f"{digits[0]}تونس{digits[1]}"
    
    return ""

# --- Nettoyage caractères interdits Firebase ---
def sanitize_plate_text(plate):
    return re.sub(r'[.#$\[\]/]', '_', plate)

# --- OCR ---
reader = easyocr.Reader(['ar', 'en'], gpu=False)

# --- YOLO ---
model = YOLO("matricule_arabe2.pt")
names = {0: 'numberplate'}

# --- Vidéo et zone d’entrée ---
cap = cv2.VideoCapture("arabe4.mp4")
#arabe115  area =[(16,433), (65,519), (344, 519), (360, 439)]
#arabe14   kol mara kfhh  area =[(16,433), (65,519), (344, 519), (360, 439)]   
#arabe13    detection coorecte area =[(61,492), (48,523), (442, 513), (431, 440)]
#arabe12#    detectation correcte area =[(33,417), (61,492), (298, 508), (313, 436)]
#arabe11#   area =[(13,486), (35,547), (35, 547), (436, 574)]
#arabe10#    dection coorecte marat ghalta wmbaad shiha area =[(18,354), (2,462), (475, 426), (477, 348)]
#arabe9#    no detection vd mouch rakah jimlaa area =[(76,437), (95, 472), (270, 464), (274, 428)]
#arabe8#   dectation avec des erreur de nbre area =[(72,688), (110, 744), (414, 731), (420, 666)]
#arabe7#   dectation avec des erreur de nbre  area =[(72,688), (110, 744), (414, 731), (420, 666)]
#arabe6   dectation avec des erreur de nbre #area =[(162,455), (168, 488), (311, 486), (319, 452)]  
#arabe5#   dectation avec des erreur de nbre area =[(5,455), (3, 618), (445, 637), (470, 508)]
#arabe4#  detectation correcte   area =[(15, 368), (34, 476), (442, 471), (454, 360)]
#arabe3#  detectation correcte area =[(15, 368), (34, 476), (442, 471), (454, 360)]
# area =[(194, 204), (207, 292), (542, 330), (574, 251)]#arabe2# detectation correcte 
#arabe1# detecter correctement  area = [(192, 284), (210, 358), (534, 374), (519, 308)]

# area = [(29, 378), (16, 456), (1015, 451), (965, 378)]
# area = [(56, 207), (57, 313), (937, 312), (887, 189)]
# area = [(33,295), (29, 390), (965, 390), (950,300)]
area =[(29, 387), (17, 489), (466, 498), (460, 390)]
# area =[(4, 526), (1, 626), (474, 627), (460, 522)]
# --- Variables principales ---
total_places = 4
place_disponible = total_places
entry_log = {}
last_seen = {}
DETECTION_COOLDOWN = 5  # secondes
valid_plates, name_dict = load_valid_plates()
# --- Initialiser parking_status s'il a été supprimé ---
today = date.today().isoformat()
status_ref = db.reference(f'parking_status/{today}')
if not status_ref.get():
    status_ref.set({
        "last_detected_plate": "Aucun",
        "available_spots": place_disponible
    })
#initialisation de parking 
response_ref = db.reference('parking/response')

# --- Boucle principale ---
while True:
    success, frame = cap.read()
    if not success:
        break
    
    # frame = cv2.resize(frame, (1020, 500))
    results = model.predict(frame, save=False)[0]
    now = datetime.now()
    today = date.today().isoformat()

    for box in results.boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        class_id = int(box.cls[0])
        name = names[class_id]
        cx, cy = (x1 + x2) // 2, (y1 + y2) // 2

        if name == 'numberplate' and cv2.pointPolygonTest(np.array(area, np.int32), (cx, cy), False) >= 0:
            # --- Recadrage plus précis + padding optionnel ---
            pad = 5
            x1_pad = max(0, x1 - pad)
            y1_pad = max(0, y1 - pad)
            x2_pad = min(frame.shape[1], x2 + pad)
            y2_pad = min(frame.shape[0], y2 + pad)

            plate_img = frame[y1_pad:y2_pad, x1_pad:x2_pad]
            plate_gray = cv2.cvtColor(plate_img, cv2.COLOR_BGR2GRAY)

            # --- OCR ---
            result = reader.readtext(plate_gray, detail=0)
            plate_text = ''.join(result).replace(" ", "").upper()
            plate_text = correct_ocr_errors(plate_text)

            # Ajout de "تونس" si absent
            if "تونس" not in plate_text:
                match = re.match(r"(\d{1,3})(\d{3,4})", plate_text)
                if match:
                    plate_text = f"{match.group(1)}تونس{match.group(2)}"

            if not plate_text or len(plate_text) < 9:
                continue
        
            plate_text = sanitize_plate_text(plate_text)

            # --- Anti-spam (cooldown) ---
            last_time = last_seen.get(plate_text, None)
            if last_time and (now - last_time).total_seconds() < DETECTION_COOLDOWN:
                continue
            last_seen[plate_text] = now

            color = (0, 0, 255)

            try:
                if plate_text in valid_plates:
                    employee_id = valid_plates[plate_text]
                    
                    if plate_text not  in entry_log:
                        # --- Entrée ---
                        place_disponible = max(place_disponible - 1, 0)
                        entry_log[plate_text] = now.strftime("%H:%M:%S")
                
                        response_ref.set({
                        "plate": plate_text,
                        "authorized": plate_text in valid_plates,
                        "available_spots": place_disponible,
                        "timestamp": now.strftime("%H:%M:%S"),
                        "handled": False,
                        "direction": "entry",  # ou "exit"
                        "employee_name": name_dict.get(plate_text, "")  # dictionnaire des noms associés aux plaques
                        })
                        
                        db.reference(f'parking_access/{today}/authorized/{plate_text}').set({
                            "employee_id": employee_id,
                            "entry_time": now.strftime("%H:%M:%S")
                        })
                        db.reference(f'parking_status/{today}').set({
                            "last_detected_plate": plate_text,
                            "available_spots": place_disponible
                        })
                        
                    
                        db.reference('history').push({
                            "plate": plate_text,
                            "authorized": True,
                            "date": today,
                            "entry_time": now.strftime("%H:%M:%S"),
                            "exit_time": None
                        })
                        color = (0, 255, 0)
                    else:
                        # --- Sortie ---
                        place_disponible = min(place_disponible + 1, total_places)
                        exit_time = now.strftime("%H:%M:%S")
                        response_ref.set({
                        "plate": plate_text,
                        "authorized": True,
                        "available_spots": place_disponible,
                        "timestamp": now.strftime("%H:%M:%S"),
                        "handled": False,
                        "direction": "exit",
                        "employee_name": name_dict.get(plate_text, "")
                        })
                        history_data = db.reference('history').get()
                        if history_data:
                            for key, val in history_data.items():
                                if val.get("plate") == plate_text and val.get("date") == today and val.get("exit_time") is None:
                                    db.reference('history').child(key).update({"exit_time": exit_time})
                                    break
                        entry_log.pop(plate_text, None)
                        db.reference(f'parking_access/{today}/authorized/{plate_text}').update({
                            "exit_time": exit_time
                        })
                        db.reference(f'parking_status/{today}').set({
                            "last_detected_plate": plate_text,
                            "available_spots": place_disponible
                        })
                       
                    

                        color = (255, 165, 0)
                else:
                    # --- Non autorisée ---
                    history = db.reference('history').get()
                    response_ref.set({
                    "plate": plate_text,
                    "authorized": False,
                    "available_spots": place_disponible,
                    "timestamp": now.strftime("%H:%M:%S"),
                    "handled": False,
                    "direction": "entry",  
                    "employee_name": ""
                    })
                    already_logged = False
                    if history:
                        for val in history.values():
                            if val.get("plate") == plate_text and val.get("date") == today and not val.get("authorized", False):
                                already_logged = True
                                break
                    if not already_logged:
                        db.reference(f'parking_access/{today}/unauthorized/{plate_text}').set({
                            "plate_number": plate_text,
                            "detection_time": now.strftime("%H:%M:%S")
                        })
                        db.reference('history').push({
                            "plate": plate_text,
                            "authorized": False,
                            "date": today,
                            "time": now.strftime("%H:%M:%S")
                        })
                        
                        
            except Exception as e:
                print(f"Erreur Firebase pour la plaque {plate_text} : {e}")
                continue

            # --- Affichage graphique ---
            cvzone.putTextRect(frame, plate_text, (x1, y1 - 15), scale=2, thickness=2, colorR=color)
            cvzone.cornerRect(frame, (x1, y1, x2 - x1, y2 - y1), colorC=color)

    # --- Affichage places disponibles ---
    cv2.polylines(frame, [np.array(area, np.int32)], True, (255, 0, 0), 2)
    cv2.putText(frame, f'Places disponibles: {place_disponible}/{total_places}', (30, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
    cv2.imshow("Detection", frame)

    if cv2.waitKey(1) == ord('q'):
        break

# --- Nettoyage ---
cap.release()
cv2.destroyAllWindows()
