Shared Android hymn parchment asset

Place the image at:
  platform/shared/src/androidMain/assets/shhimo-parchment.jpg

Android merges assets from the shared library into each consuming app.
Both Shhimo and Occasions can load this image with assets.open("shhimo-parchment.jpg").

The Shhimo hymn screen currently enables this background; the Occasions screen
is unchanged until its presentation is explicitly configured to use it.
