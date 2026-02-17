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

# Ignore missing Gemalto JPEG2000 classes used by PdfBox-Android
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder

# Prevent Moshi from losing property names for GitHub models
-keepclassmembers class off.kys.github_app_updater.model.github.** {
    <fields>;
    <init>(...);
}

# Keep the data classes and their members (fields/methods)
-keep class off.kys.github_app_updater.model.github.** { *; }

# Specifically prevent Moshi from losing the types inside Lists/Collections
-keepattributes Signature, EnclosingMethod, InnerClasses, RuntimeVisibleAnnotations

# For using Moshi's @Json annotations, keep those too
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault