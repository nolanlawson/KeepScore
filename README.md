KeepScore
=========

Author
--------
Nolan Lawson

License
---------
[WTFPL][1], although attribution would be nice.

Overview
----------

![Screenshot][2]

KeepScore is a free and open-source score-keeping app for Android. 
Pull requests are always welcome.  I'll try to incorporate your changes into the app 
on the Google Play Store as soon as I can.

Download
----------

KeepScore may be downloaded from [the Google Play Store][4].  

You can also find a direct APK download link from [the KeepScore page on my web site][3].

More details about KeepScore can also be found in [my blog posts][10].

Build it yourself
------------------

KeepScore is built using [Maven][8].

KeepScore has a dependency, [AmbilWarna][11], which is not avilable in Maven.  So you'll have to first install it manually:

```
git clone https://code.google.com/p/yuku-android-util/
cd yuku-android-util/AmbilWarna
zip -r AmbilWarna.zip *.xml *.properties res src
mvn install:install-file -Dfile=AmbilWarna.zip -DgroupId=yuku.ambilwarna -DartifactId=yuku.ambilwarna-apk -Dversion=1.0 -Dpackaging=apklib
```

Then, to build, install, and run KeepScore on your device, simply cd to the ```KeepScore/``` directory and do:

```mvn clean install android:deploy android:run```

To run the unit tests, cd to the ```KeepScoreTest/``` directory and do:

```mvn clean install```

You must have Maven 3.0.3+ installed in order for this to work.

Add a translation
-------------------

KeepScore is currently localized into English, French, Japanese, Hebrew, Italian, and German.  For the benefit of
board gamers around the world, though, I'm
always happy to get new translations!

### French and Japanese

I manage these myself.  I'm not a native speaker, though, so if you have corrections to make, please fork these files:

- [French](https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-fr/strings.xml)
- [Japanese](https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-ja/strings.xml)

### Italian, Hebrew, and German

These translations are only needed when new feature are added.  Thanks to those who helped with the 1.3.0 translations!

### Adding a new language

New languages are always welcome!  Based on the language statistics, it would be nice to have 
Dutch, Korean, Russian, Portoguese, and Spanish.

All you need to do is add some additional files, fork the project on GitHub, and send me a pull request. 
And thanks to GitHub [file creation][7], you can do all of this through the web interface!

For instance,
if your language is Esperanto (ISO code 'eo'), you would need to create the following files:

```
KeepScore/res/values-eo/strings.xml           # Main translations
KeepScore/res/values-eo/dimensions.xml        # Modifies width of the time period overlay (e.g. "Yesterday", 
                                              # "The Past Week") for languages with long words, e.g. French
KeepScore/res/raw-eo/version_and_credits.xml  # Page shown in "About KeepScore"
documentation/description-eo.txt              # Play Store description
documentation/donate/description-eo.txt       # Play Store description (Donate version)
```

(Again, please read the GitHub page on [file creation][7] to understand how to do this.)

I maintain the English, French, and Japanese translations myself.
So you can look to these translations as an example:

<table border='0'>
<tr>
<td align='center'><b>English</b></td>
<td align='center'><b>French</b></td>
<td align='center'><b>Japanese</b></td>
</tr>
<tr>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values/strings.xml'>strings.xml</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-fr/strings.xml'>strings.xml</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-ja/strings.xml'>strings.xml</a></td>
</tr>
<tr>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values/dimensions.xml#L57'>dimensions.xml</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-fr/dimensions.xml'>dimensions.xml</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values-ja/dimensions.xml'>dimensions.xml</a></td>
</tr>
<tr>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/raw/version_and_credits.htm'>version_and_credits.htm</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/raw-fr/version_and_credits.htm'>version_and_credits.htm</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/raw-ja/version_and_credits.htm'>version_and_credits.htm</a></td>
</tr>
<tr>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/description-en.txt'>description-en.txt</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/description-fr.txt'>description-fr.txt</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/description-ja.txt'>description-ja.txt</a></td>
</tr>
<tr>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/donate/description-en.txt'>donate/description-en.txt</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/donate/description-fr.txt'>donate/description-fr.txt</a></td>
<td align='center'><a href='https://github.com/nolanlawson/KeepScore/blob/master/documentation/donate/description-ja.txt'>donate/description-ja.txt</a></td>
</tr>
</table>

Note that not all strings in ```strings.xml``` need to be translated.  If you look at [the English strings.xml file][6],
you'll see that I've helpfully marked constant strings with the prefix ```CONSTANT_```.  Constants do not need
to be translated.

A helpful tool for finding missing translations is the [Android Localization Helper][9].  

To ignore constants while using it, you can
run:

```/path/to/localization/helper/bin/alh.sh /path/to/keepscore/ | grep -v CONSTANT```

Please also tell me what name and email address you would like me to use in the "About" section, so I 
can give you proper credit.  Or just fork and modify the file ```KeepScore/res/raw/translations.htm```
yourself.  Thanks!

[1]: http://sam.zoy.org/wtfpl/
[2]: http://nolanwlawson.files.wordpress.com/2013/01/device-2013-01-06-141649.png?w=450
[3]: http://nolanlawson.com/apps/#keepscore
[4]: https://play.google.com/store/apps/details?id=com.nolanlawson.keepscore
[5]: http://actionbarsherlock.com/
[6]: https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values/strings.xml
[7]: https://github.com/blog/1327-creating-files-on-github
[8]: http://maven.apache.org/
[9]: https://github.com/4e6/android-localization-helper
[10]: http://nolanlawson.com/tag/keepscore/
[11]: https://code.google.com/p/android-color-picker/
