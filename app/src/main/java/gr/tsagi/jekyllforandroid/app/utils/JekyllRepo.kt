package gr.tsagi.jekyllforandroid.app.utils

import android.os.AsyncTask

import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.service.RepositoryService

import java.io.IOException
import java.util.concurrent.ExecutionException

/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 1/29/14
\* Time: 15:14
\*/

class JekyllRepo {

    fun getName(user: String): String? {

        try {
            return CheckAllRepos().execute(user).get()
        } catch (e: InterruptedException) {

            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        return null
    }

    private inner class CheckAllRepos : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {

            val user = params[0]
            var name: String? = null

            val repositoryService = RepositoryService()

            var repositories: List<Repository>? = null
            try {
                repositories = repositoryService.getRepositories(user)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (repository in repositories!!) {
                if (repository.name.contains(user + ".github.")) {
                    name = repository.name
                    break
                }
                if (repository.name.contains(user.toLowerCase() + ".github.")) {
                    name = repository.name
                    break
                }
            }
            return name!!
        }

    }
}
