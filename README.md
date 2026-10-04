# Sistema de Monitoreo y Control de Temperatura IoT

Proyecto académico de integración IoT desarrollado con comunicación bidireccional y persistencia en tiempo real.

## Arquitectura del Sistema
1. **Sensor y Captura (Wokwi):** ESP32 conectado a sensor DHT22 y actuador LED para alerta local.
2. **Broker MQTT (HiveMQ):** Publicación y suscripción mediante el topic `iot/ruand/temperatura`.
3. **Bridge / Procesamiento (Raspberry Pi):** Script en Python (`bridge.py`) que consume las lecturas MQTT, procesa los datos y los envía a la nube.
4. **Base de Datos en la Nube (Firebase):** Firebase Realtime Database para sincronización reactiva y Firebase Authentication para control de acceso.
5. **Aplicación Móvil (Android):** Desarrollada en Kotlin con Jetpack Compose para visualización de temperatura en vivo, alertas y control remoto bidireccional en dos o más dispositivos.

## Cumplimiento de Estándares de Seguridad (ISO 27400)
- **Cifrado en Tránsito:** Toda la transmisión de datos hacia y desde Firebase se realiza bajo protocolos TLS/HTTPS, garantizando confidencialidad e integridad frente a ataques de intermediario (Man-in-the-Middle).
- **Principio de Mínimos Privilegios:** La aplicación Android declara únicamente los permisos estrictamente necesarios en su manifiesto (`android.permission.INTERNET` y `android.permission.ACCESS_NETWORK_STATE`).
- **Autenticación Robusta:** El acceso a los datos del panel requiere credenciales validadas a través de Firebase Authentication.
