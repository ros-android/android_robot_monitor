# Bash

../gradlew clean debug
if [ $? -eq 0 ]
 then
  adb uninstall org.ros.android.robot_monitor
  adb install bin/MainActivity-debug.apk
  adb shell am start -n org.ros.android.robot_monitor/org.ros.android.robot_monitor.MonitorApplication
  adb logcat -c
  adb logcat
fi
