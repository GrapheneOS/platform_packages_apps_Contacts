package com.android.contacts.domain.debug.usecase

import android.graphics.Bitmap
import android.graphics.Color
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Relation
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.android.contacts.domain.debug.model.DebugDataConstants
import com.android.contacts.domain.debug.model.TestContact
import com.android.contacts.domain.debug.model.TestContact.ValueWithType
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

internal fun interface GenerateTestContact {
    operator fun invoke(): TestContact
}

internal class GenerateTestContactImpl @Inject constructor(
    private val random: Random,
) : GenerateTestContact {
    override fun invoke(): TestContact {
        val name = randomName()
        val identity = if (shouldAddExtraProperty()) randomIdentity(name.given) else null

        return TestContact(
            phones = rangeUpTo(MAX_PHONES_COUNT).map { randomPhone() },
            name = name,
            nickname = if (shouldAddExtraProperty()) randomNickname() else null,
            emails = rangeUpTo(MAX_EMAILS_COUNT).map { randomEmail(name.given) },
            postal = if (shouldAddExtraProperty()) randomPostal() else null,
            organization = if (shouldAddExtraProperty()) SURNAMES.random(random) else null,
            relation = if (shouldAddExtraProperty()) RELATIONS.random(random) else null,
            website = if (shouldAddExtraProperty()) randomWebsite(name.given) else null,
            event = if (shouldAddExtraProperty()) randomEvent() else null,
            im = if (shouldAddExtraProperty()) randomIm(name.given) else null,
            sipAddress = if (shouldAddExtraProperty()) randomSipAddress(name.given) else null,
            identityValue = identity?.first,
            identityNamespace = identity?.second,
            note = if (shouldAddExtraProperty()) randomNote() else null,
            photo = if (shouldAddExtraProperty()) randomPhoto() else null,
        )
    }

    private fun rangeUpTo(times: Int): IntRange {
        return 1..random.nextInt(1..times)
    }

    private fun shouldAddExtraProperty(): Boolean {
        return random.nextBoolean()
    }

    private fun randomName(): TestContact.Name {
        val givenName = NAMES.random(random)
        val middleName = if (shouldAddExtraProperty()) NAMES.random(random) else null
        val familyName = if (shouldAddExtraProperty()) SURNAMES.random(random) else null
        val displayName = if (shouldAddExtraProperty()) {
            listOfNotNull(
                givenName,
                middleName,
                familyName,
            ).joinToString(" ")
        } else {
            null
        }
        return TestContact.Name(
            given = givenName,
            middle = middleName,
            family = familyName,
            display = displayName,
        )
    }

    private fun randomPhone(): ValueWithType<String> {
        val number = DebugDataConstants.PHONE_PREFIX +
            random.nextInt(999_999).toString().padStart(6, '0')
        val type = PHONE_TYPES.random(random)
        return ValueWithType(number, type)
    }

    private fun randomNickname(): ValueWithType<String> {
        return ValueWithType(
            (NAMES + SURNAMES).random(random),
            NICKNAME_TYPES.random(random),
        )
    }

    private fun randomEmail(name: String): ValueWithType<String> {
        return ValueWithType(
            randomEmailValue(name),
            EMAIL_TYPES.random(random),
        )
    }

    private fun randomEmailValue(name: String): String {
        return "${name.lowercase()}@${DOMAINS.random(random)}"
    }

    private fun randomPostal(): ValueWithType<TestContact.Postal> {
        return ValueWithType(
            TestContact.Postal(
                city = CITIES.random(random),
                country = COUNTRIES.random(random),
            ),
            POSTAL_TYPES.random(random),
        )
    }

    private fun randomWebsite(name: String): ValueWithType<String> {
        return ValueWithType(
            "https://${name.lowercase()}.${DOMAINS.random(random)}",
            WEBSITE_TYPES.random(random),
        )
    }

    private fun randomEvent(): ValueWithType<String> {
        val daysToAdd = random.nextLong(-EVENT_DAYS_RANGE, EVENT_DAYS_RANGE)
        return ValueWithType(
            LocalDate.now().plusDays(daysToAdd).toString(),
            EVENT_TYPES.random(random),
        )
    }

    private fun randomIm(name: String): ValueWithType<TestContact.Im> {
        return ValueWithType(
            TestContact.Im(
                data = name.lowercase(),
                protocol = IM_PROTOCOLS.random(random),
            ),
            IM_TYPES.random(random),
        )
    }

    private fun randomSipAddress(name: String): ValueWithType<String> {
        return ValueWithType(
            randomEmailValue(name),
            SIP_ADDRESS_TYPES.random(random),
        )
    }

    private fun randomIdentity(name: String): Pair<String, String> {
        val value = randomEmailValue(name)
        val namespace = DOMAINS.random(random).split(".").reversed().joinToString(".")
        return value to namespace
    }

    private fun randomNote(): String {
        val words = (NAMES + SURNAMES + CITIES + COUNTRIES)
        return words
            .shuffled()
            .take(random.nextInt(1, NOTE_MAX_WORDS))
            .joinToString(" ")
    }

    private fun randomPhoto(): TestContact.Photo {
        val bitmap = createBitmap(PHOTO_SIZE, PHOTO_SIZE, Bitmap.Config.ARGB_8888)
        for (x in 0 until PHOTO_SIZE) {
            for (y in 0 until PHOTO_SIZE) {
                bitmap[x, y] = randomColor()
            }
        }
        val bos = ByteArrayOutputStream()
        bos.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, BITMAP_COMPRESS_QUALITY, outputStream)
        }
        bitmap.recycle()
        return TestContact.Photo(bos.toByteArray())
    }

    private fun randomColor(): Int {
        return Color.HSVToColor(
            arrayOf(random.nextInt(COLOR_RANGE).toFloat(), 1f, 1f).toFloatArray(),
        )
    }

    companion object {
        private const val MAX_PHONES_COUNT = 3
        private const val MAX_EMAILS_COUNT = 3
        private const val EVENT_DAYS_RANGE = 1000L
        private const val NOTE_MAX_WORDS = 24
        private const val PHOTO_SIZE = 3
        private val COLOR_RANGE = 0..355
        private const val BITMAP_COMPRESS_QUALITY = 100 // percentage

        private val NAMES = listOf(
            "Alder", "Alf", "Alheri", "Alket", "Amadioha", "Amram", "Amália", "Amēlija",
            "Anastasiya", "Anatolijs", "Angie", "Anke", "Apli", "Apollodoros", "Arun", "Ashur",
            "Bani", "Bernardo", "Carola", "Chimwemwe", "Christiaan", "Dorinda", "Ernst",
            "Euthymios", "Fawzi", "Gerhild", "Guy", "Hanae", "Hopcyn", "Hoshiko", "Hrothgar",
            "Ina", "Irnerius", "Iulian", "Jeyden", "Jimmu", "Jonatan", "Justýna", "Jón", "Kamil",
            "Kannon", "Khazhak", "Kyrilla", "Lucas", "Lughaidh", "Manawydan", "Margaux",
            "Marilène", "Melaina", "Mihăiță", "Miko", "Milagrosa", "Miska", "Mislav", "Natalina",
            "Nerijus", "Nevaeh", "Nina", "Nisa", "Phlegon", "Pietronella", "Prosper", "Pryderi",
            "Rama", "Ramakanta", "Renatas", "Roland", "Royston", "Rudi", "Saulos", "Severino",
            "Sharmila", "Shaylyn", "Signý", "Silvia", "Siro", "Slobodan", "Sokrates", "Sperantia",
            "Stepan", "Stephani", "Suero", "Terah", "Tobias", "Toni", "Varlam", "Victor",
            "Vikrama", "Vitold", "Waldeburg", "Yehoshafat", "Yisrael", "Yuri", "Zhasulan",
            "Íñigo", "Čestislav",
        )
        private val SURNAMES = listOf(
            "Aarle", "Abrams", "Agnelli", "Ahmad", "Aiza", "Albanesi", "Andrzejewska", "Armati",
            "Ayala", "Baart", "Bager", "Bakó", "Bartoš", "Beckers", "Benoit", "Bower", "Brierley",
            "Brown", "Charbonneau", "Chilikova", "Clemente", "Cochran", "Császár", "Dalí",
            "Demetriou", "Ergeshov", "Fosse", "Frank", "Geary", "Gentile", "Georgiev", "Giannaki",
            "Glas", "Habich", "Halmi", "Hassan", "Hendrix", "Hepburn", "Hidayat", "Hill", "Hilton",
            "Honchar", "Horvatinčić", "Hussain", "Ibragimov", "Jabłońska", "Jansens", "Kartal",
            "Knez", "Koppel", "Langley", "Lauwens", "Macháňová", "Maekawa", "McCrae", "McGowan",
            "McKellar", "McNab", "Medeiros", "Moffett", "Morrish", "Musaev", "Niemec", "Novak",
            "Novikov", "Oppenheimer", "Padovan", "Parrish", "Pawlitzki", "Picasso", "Protz",
            "Putnam", "Rana", "Samson", "Sappington", "Schäfer", "Sheridan", "Silva", "Souza",
            "Spanos", "Stankić", "Stauss", "Steele", "Sydorenko", "Szymańska", "Tanguy",
            "Thompson", "Toloni", "Tähtinen", "Underwood", "Van Aarle", "Van der Laar",
            "Warszawska", "Wyrzyk", "Yılmaz", "Ó Fionnagáin", "Čížiková", "Žagar",
        )
        private val PHONE_TYPES = listOf(
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME,
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK,
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME,
            ContactsContract.CommonDataKinds.Phone.TYPE_PAGER,
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER,
            ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK,
            ContactsContract.CommonDataKinds.Phone.TYPE_CAR,
            ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN,
            ContactsContract.CommonDataKinds.Phone.TYPE_ISDN,
            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN,
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX,
            ContactsContract.CommonDataKinds.Phone.TYPE_RADIO,
            ContactsContract.CommonDataKinds.Phone.TYPE_TELEX,
            ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD,
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE,
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER,
            ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT,
            ContactsContract.CommonDataKinds.Phone.TYPE_MMS,
            ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM,
            null,
        )
        private val NICKNAME_TYPES = listOf(
            ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT,
            ContactsContract.CommonDataKinds.Nickname.TYPE_OTHER_NAME,
            ContactsContract.CommonDataKinds.Nickname.TYPE_MAIDEN_NAME,
            ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM,
            null,
        )
        private val DOMAINS = listOf(
            "example.com", "example.org", "example.net", "example.edu", "altostrat.com",
            "examplepetstore.com", "example-pet-store.com", "myownpersonaldomain.com",
            "my-own-personal-domain.com", "cymbalgroup.com",
        )
        private val EMAIL_TYPES = listOf(
            ContactsContract.CommonDataKinds.Email.TYPE_HOME,
            ContactsContract.CommonDataKinds.Email.TYPE_WORK,
            ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
            ContactsContract.CommonDataKinds.Email.TYPE_MOBILE,
            ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM,
            null,
        )
        private val POSTAL_TYPES = listOf(
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM,
            null,
        )
        private val CITIES = listOf(
            "Adelaide", "Algiers", "Almaty", "Amsterdam", "Apia", "Athens", "Auckland", "Baghdad",
            "Baku", "Bangkok", "Belgrade", "Berlin", "Bogota", "Bratislava", "Brisbane",
            "Brussels", "Bucharest", "Budapest", "Buenos Aires", "Cairo", "Caracas", "Casablanca",
            "Chicago", "Chihuahua", "Chongqing", "Colombo", "Copenhagen", "Darwin", "Denver",
            "Dhaka", "Dublin", "Fakaofo", "Fiji", "Godthab", "Guam", "Guatemala", "Guyana",
            "Halifax", "Harare", "Helsinki", "Hobart", "Hong Kong", "Honolulu", "Indianapolis",
            "Irkutsk", "Istanbul", "Jakarta", "Jerusalem", "Johannesburg", "Juneau", "Kabul",
            "Kamchatka", "Karachi", "Kathmandu", "Kiev", "Kolkata", "Krasnoyarsk", "Kuala Lumpur",
            "Kuwait", "La Paz", "Lima", "Lisbon", "Ljubljana", "London", "Los Angeles", "Madrid",
            "Magadan", "Majuro", "Mazatlan", "Melbourne", "Mexico City", "Minsk", "Monrovia",
            "Monterrey", "Moscow", "Muscat", "Nairobi", "New York", "Noumea", "Novosibirsk",
            "Pago Pago", "Paris", "Perth", "Phoenix", "Port Moresby", "Prague", "Rangoon",
            "Regina", "Riga", "Riyadh", "Rome", "Santiago", "Sarajevo", "Seoul", "Shanghai",
            "Singapore", "Skopje", "Sofia", "South Georgia", "St Johns", "Stockholm", "Sydney",
            "São Miguel", "São Paulo", "São Vicente", "Taipei", "Tallinn", "Tashkent", "Tbilisi",
            "Tehran", "Tijuana", "Tokyo", "Tongatapu", "Ulaanbaatar", "Urumqi", "Vienna",
            "Vilnius", "Vladivostok", "Warsaw", "Yakutsk", "Yekaterinburg", "Yerevan", "Zagreb",
        )
        private val COUNTRIES = Locale.availableLocales()
            .map { it.displayCountry }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
        private val RELATIONS = listOf(
            ValueWithType("Assistant", Relation.TYPE_ASSISTANT),
            ValueWithType("Brother", Relation.TYPE_BROTHER),
            ValueWithType("Child", Relation.TYPE_CHILD),
            ValueWithType("Domestic partner", Relation.TYPE_DOMESTIC_PARTNER),
            ValueWithType("Father", Relation.TYPE_FATHER),
            ValueWithType("Friend", Relation.TYPE_FRIEND),
            ValueWithType("Manager", Relation.TYPE_MANAGER),
            ValueWithType("Mother", Relation.TYPE_MOTHER),
            ValueWithType("Parent", Relation.TYPE_PARENT),
            ValueWithType("Partner", Relation.TYPE_PARTNER),
            ValueWithType("Referred by", Relation.TYPE_REFERRED_BY),
            ValueWithType("Relative", Relation.TYPE_RELATIVE),
            ValueWithType("Sister", Relation.TYPE_SISTER),
            ValueWithType("Spouse", Relation.TYPE_SPOUSE),
            ValueWithType("Developer", Relation.TYPE_CUSTOM),
        )
        private val WEBSITE_TYPES = listOf(
            ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE,
            ContactsContract.CommonDataKinds.Website.TYPE_BLOG,
            ContactsContract.CommonDataKinds.Website.TYPE_PROFILE,
            ContactsContract.CommonDataKinds.Website.TYPE_HOME,
            ContactsContract.CommonDataKinds.Website.TYPE_WORK,
            ContactsContract.CommonDataKinds.Website.TYPE_FTP,
            ContactsContract.CommonDataKinds.Website.TYPE_OTHER,
            ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM,
        )
        private val EVENT_TYPES = listOf(
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY,
            ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY,
            ContactsContract.CommonDataKinds.Event.TYPE_OTHER,
            ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM,
        )
        private val IM_TYPES = listOf(
            ContactsContract.CommonDataKinds.Im.TYPE_HOME,
            ContactsContract.CommonDataKinds.Im.TYPE_WORK,
            ContactsContract.CommonDataKinds.Im.TYPE_OTHER,
            ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM,
        )
        private val IM_PROTOCOLS = listOf(
            "AIM", "MSN", "Yahoo", "Skype", "QQ", "Google Talk", "ICQ", "Jabber", "Netmeeting",
        )
        private val SIP_ADDRESS_TYPES = listOf(
            ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME,
            ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK,
            ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER,
            ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM,
        )
    }
}
