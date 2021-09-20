# Introduction
App created by Aaron Schwartz. Contact via ams1347@uncw.edu or schwartz.m.aaron@gmail.com
My final project for CSC-315 [Mobile App Development].
As a video game collector myself, the original pitch was an app that helps video game collectors keep track of their personal video game collections. However, I realized it could really be used for any kind of collection. It uses Google Firebase to keep track of the overall database for games, as well as user's individual collections. It also implements a RecyclerView interface, along with image uploading, neither of which were easy to get working.
This app turned into a passion project for me, and it's still something I'm rather proud of.

#How does it work?
After you create an account (the app uses Google Firebase to authenticate and keep track of users), you'll be taken to a default homepage screen.
This is a collective database of all games that users have uploaded. If you see a video game that you want, you can add it to your personal collection. If a game you want to add to your personal collection is not available, you can add it yourself. Once you've added your own game, it will also be present in the public database for all members to be able to add to their collection. The idea is users will slowly but surely build up an extensive database of all games that new users can easily pull from.

#Known bugs/issues
* App crashes when selecting "My Collection" menu item when the user does not have any items in their personal collection yet.

#Improvements to be made
From Dr. Lucas Layman:
* "Would like to see the ability to create an 'empty' collection from the Collections activity."
* "Would be a nice feature when adding a Game to a Collection if the Snackbar had an action to Go To Collection. Or to "Undo". Or both! Remember to permit easy reversal of actions."
* "'My Collections' is such an important place to go that I think you should have it as an Action Button in the app bar, not just a menu item."

#Accolades for encouragement
* "Overall, really good and clean. One of the best projects from this or any semester!" - Dr. Lucas Layman
