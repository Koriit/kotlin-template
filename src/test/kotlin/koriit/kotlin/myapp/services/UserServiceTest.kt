package koriit.kotlin.myapp.services

import koriit.kotlin.myapp.TestApplication
import org.hamcrest.Matchers.not
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class UserServiceTest : KodeinAware {

    override val kodein = TestApplication()

    private val service: UsersService by instance()
}
