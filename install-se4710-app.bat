cd F:\Workspace\AndroidStudio-sdk\platform-tools
adb root
adb remount
adb push F:\Workspace\AndroidstudioProjects\SoftScanServiceNew\se4710\build\outputs\apk\debug\se4710service_v1.9.2.apk /system/priv-app/Se4710/
adb reboot
cd F:\Workspace\AndroidstudioProjects\SoftScanServiceNew