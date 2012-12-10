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

KeepScore is a free and open-source score-keeping app for Android.  Feel free to file bugs on the GitHub page or branch the code yourself!  Just send me a merge request and I'll try to incorporate your changes into the app on the Android Market.

More details about the app can be found on [my website][3] or the [Google Play Store][4].

Build it yourself
------------------

KeepScore requires the [Action Bar Sherlock][5] library in order to be built.  The easiest thing to do is just to download
Action Bar Sherlock to a directory called ```ActionBarSherlock/``` in your workspace, then add it into Eclipse
as an existing project.  The Eclipse project for KeepScore will be looking for a directory with this exact name.

Add a translation
-------------------

KeepScore is currently localized into English, French, Japanese, and German.  For the benefit of
board gamers around the world, though, I'm
always happy to get new translations!

All you need to do is add some additional files, fork the project on GitHub, and send me a pull request. 
And thanks to GitHub file creation[7], you can do all of this through the web interface.

For instance,
if your language is Esperanto (ISO code 'eo'), you would need to add the following files:

```
documentation/description-eo.txt              # Play Store description
documentation/updates-eo.txt                  # Play Store "updates" section (optional! I'm not sure I will maintain this)
KeepScore/res/values-eo/strings.xml           # main translations
KeepScore/res/values-eo/dimensions.xml        # useful if Esperanto has really long words, see e.g. French
KeepScore/res/raw-eo/version_and_credits.xml  # page shown in "About KeepScore"
```

Note that not all strings in ```strings.xml``` need to be translated.  If you look at [the English strings.xml file][6],
you'll see that I've helpfully divided it into "non-translatable strings" and "translatable" strings.  For any non-English
```strings.xml``` file, you only have to translate the latter group.  The other ones are just constants.

Please also tell me what name and email address you would like me to use in the "About" section, so I 
can give you proper credit.  Or just fork and modify the file ```KeepScore/res/raw/translations.htm```
yourself.  Thanks!

[1]: http://sam.zoy.org/wtfpl/
[2]: http://nolanwlawson.files.wordpress.com/2012/09/device-2012-09-19-225256.png?w=252&h=450
[3]: http://nolanlawson.com/tag/keepscore
[4]: https://play.google.com/store/apps/details?id=com.nolanlawson.keepscore
[5]: http://actionbarsherlock.com/
[6]: https://github.com/nolanlawson/KeepScore/blob/master/KeepScore/res/values/strings.xml
[7]: https://github.com/blog/1327-creating-files-on-github