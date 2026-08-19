/**
 * Precompiled [contacts.licenses.gradle.kts][Contacts_licenses_gradle] script plugin.
 *
 * @see Contacts_licenses_gradle
 */
public
class Contacts_licensesPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Contacts_licenses_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
