Jekyll for Android
=========================

A very simple client to post on a Jekyll blog from your phone.

It commits a `yyyy-mm-dd-title.md` file to the `_posts/` directory of your Jekyll's blog.

The application uses the [GitHub API](https://developer.github.com/).

##News *11/10/2013* :
Today I added the feature to **view posts**.
It is a very alpha feature but this does not affect any other features of the app.

To get it to work just **push** [this](https://raw.github.com/tsagi/tsagi.github.com/master/json/index.html) file in your `username.github.com/json/` directory and you will get a list of your posts in the app. The items on the list just open the selected post in the browser for now but I will add editing features later on.

In the future the app will get that, or probably a modified version of this file automatically on the user's repository (not without asking first of course). But for now you have to do it manually or it will crash.

You can get the latest version to directly install it on your smartphone from the link at the end of the README.

##Features:

- One time GitHub Login
- Create a post
	- Title
	- Category
	- Tags
	- Content (of course)
- Markdown Preview (using [bypass](http://uncodin.github.io/bypass/))
- If not published the post stays as a draft
- View posts

##To be done:

- Edit/Delete posts

#To compile

Download [ADT Bundle](http://developer.android.com/sdk/index.html) and [NDK Plugin](http://developer.android.com/tools/sdk/ndk/index.html) and Import the Project.

==========

#Download:

Download the `.apk` file [here](http://cl.ly/0E46040m1m2U).

The client is in __Alpha__ stage.

Please fork it, test it and come back with issues :)
