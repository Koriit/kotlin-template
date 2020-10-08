package koriit.kotlin.myapp.api.http.requests

import koriit.kotlin.myapp.domain.User
import korrit.kotlin.ktor.controllers.patch.PatchOf

class UserPatch : PatchOf<User>() {
    var name by patchOf(User::name)
    var age by patchOf(User::age)
}
