# Keep Android framework entry points declared in the manifest.
-keep class pl.mora.app.integration.OverlayService { *; }

# JSONObject-based API parsing uses Android platform classes and needs no model keep rules.
