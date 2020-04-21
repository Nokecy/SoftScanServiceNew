cd F:\Workspace\AndroidStudio-sdk\platform-tools
adb root
adb remount
adb push F:\Workspace\AndroidstudioProjects\SoftScanServiceNew\cm60\build\outputs\apk\debug\Scan1Service_v1.3.apk /system/app/ScanService
adb reboot
cd F:\Workspace\AndroidstudioProjects\SoftScanServiceNew\