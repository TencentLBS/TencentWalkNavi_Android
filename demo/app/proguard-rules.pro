# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}

-keep public class com.tencent.map.engine.walk.WalkEngineJNI {
	native <methods>;
	public native <methods>;
	public static native <methods>;

	private int callback(...);
}

-keep class com.tencent.map.route.data.Route {
    <fields>;
}
-keep class com.tencent.map.engine.data.GuidanceGPSPoint {
    <fields>;
    public <methods>;
}
-keep class com.tencent.map.engine.data.GuidanceEventPoint {
   <fields>;
   public <methods>;
}
-keep class com.tencent.map.route.data.RouteSegment {
    <fields>;
    public <methods>;
}
-keep class com.tencent.map.route.data.Door {
    <fields>;
    public <methods>;
}
-keep class com.tencent.map.route.data.LandMarker {
    <fields>;
    public <methods>;
}