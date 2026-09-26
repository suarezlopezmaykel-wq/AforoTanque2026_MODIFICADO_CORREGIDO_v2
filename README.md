# AforoTanque2026

Aplicación Android offline para calcular el aforo de un tanque cilíndrico horizontal.

- Inicio con 0.00 en diámetro, largo y altura.
- Cálculo automático mientras se introducen los valores.
- Fórmula geométrica exacta del segmento circular.
- Tabla de aforo cada 1 cm.
- Botón **📥 GENERAR PDF**.
- Sin permisos de Internet.
- Java, sin librerías nativas propias; compatible con dispositivos ARMv7/32 bits que soporten Android mínimo 21.
- Icono del tanque plateado con líquido azul sobre fondo celeste.

Para compilar en GitHub Actions se instala Gradle 8.7 explícitamente y se ejecuta `assembleDebug`.
