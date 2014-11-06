SteamChat 
=========

An Android application that allows you to chat to your steam friends.

Notes
--------------
There is still a mountain of work to be done.  I paid very little attention to the UI elements however what is there will suffice for the intrim.  

Use the `com.kevelbreh.steamchat.steam2` package as your working steam protocol API.  The other is discontinued and is only left in the repo for referencing. 

[SimpleContentProvider] is being used to store the conversation history and steam friend personas. The jar of the latest release is already in the `steamchat\libs` folder.

It would be ideal to use [Butter Knife] while creating activities, fragments and views. 

Protobuf object should be generated using [Wire] because google's generation is far too heavy weight for android. 

At the moment [Android Universal Image Loader] is being used for image caching.  Investation and possible implementation should be done for [Picasso].


Screenshots (before Android 5)
--------------
![Conversations](screenshot1.png?raw=true "Conversations")
![Single Chat](screenshot2.png?raw=true "Single Chat")

Contact
--------------

Feel free to contact me on twitter using the handle `@kevelbreh`

[SimpleContentProvider]:https://github.com/xxv/SimpleContentProvider
[Butter Knife]:https://github.com/JakeWharton/butterknife
[Retrofit]:https://github.com/square/retrofit
[Picasso]:https://github.com/square/picasso
[Wire]:https://github.com/square/wire
[Android Universal Image Loader]:https://github.com/nostra13/Android-Universal-Image-Loader
