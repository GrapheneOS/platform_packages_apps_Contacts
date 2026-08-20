package com.android.contacts.domain.debug.usecase

import android.graphics.Bitmap
import android.graphics.Color
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Relation
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.android.contacts.domain.debug.model.TestContact
import com.android.contacts.domain.debug.model.TestContact.ValueWithType
import java.io.ByteArrayOutputStream
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

        return TestContact(
            phones = rangeUpTo(MAX_PHONES_COUNT).map { randomPhone() },
            givenName = givenName,
            middleName = middleName,
            familyName = familyName,
            displayName = displayName,
            nickname = if (shouldAddExtraProperty()) randomNickname() else null,
            emails = rangeUpTo(MAX_EMAILS_COUNT).map { randomEmail(givenName) },
            city = if (shouldAddExtraProperty()) CITIES.random(random) else null,
            country = if (shouldAddExtraProperty()) COUNTRIES.random(random) else null,
            organization = if (shouldAddExtraProperty()) SURNAMES.random(random) else null,
            relation = if (shouldAddExtraProperty()) RELATIONS.random(random) else null,
            website = if (shouldAddExtraProperty()) randomWebsite(givenName) else null,
            photo = if (shouldAddExtraProperty()) randomPhoto() else null,
        )
    }

    private fun rangeUpTo(times: Int): IntRange {
        return 1..random.nextInt(1..times)
    }

    private fun shouldAddExtraProperty(): Boolean {
        return random.nextBoolean()
    }

    private fun randomPhone(): ValueWithType {
        val number = TestContact.PHONE_PREFIX +
            random.nextInt(999_999).toString().padStart(6, '0')
        val type = PHONE_TYPES.random(random)
        return ValueWithType(number, type)
    }

    private fun randomNickname(): ValueWithType {
        return ValueWithType(
            (NAMES + SURNAMES).random(random),
            NICKNAME_TYPES.random(random),
        )
    }

    private fun randomEmail(name: String): ValueWithType {
        return ValueWithType(
            "${name.lowercase()}@${DOMAINS.random(random)}",
            EMAIL_TYPES.random(random),
        )
    }

    private fun randomWebsite(name: String): ValueWithType {
        return ValueWithType(
            "https://${name.lowercase()}.${DOMAINS.random(random)}",
            WEBSITE_TYPES.random(random),
        )
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
        private const val PHOTO_SIZE = 3
        private val COLOR_RANGE = 0..355
        private const val BITMAP_COMPRESS_QUALITY = 100 // percentage

        private val NAMES = listOf(
            "Alder", "Alf", "Alheri", "Alket", "Amadioha", "Amēlija", "Anastasiya", "Anatolijs",
            "Angie", "Arun", "Ashur-Bani-Apli", "Carola", "Chimwemwe", "Ernst", "Euthymios",
            "Gerhild", "Hanae", "Irnerius", "Jimmu", "Kannon", "Khazhak", "Lucas", "Lughaidh",
            "Margaux", "Marilène", "Miko", "Milagrosa", "Miska", "Mislav", "Nerijus", "Nina",
            "Phlegon", "Pietronella", "Prosper", "Pryderi", "Ramakanta", "Renatas", "Roland",
            "Royston", "Saulos", "Severino", "Shaylyn", "Siro", "Slobodan", "Sokrates", "Stepan",
            "Terah", "Toni", "Varlam", "Victor", "Vikrama", "Vitold", "Waldeburg", "Zhasulan",
            "Čestislav",
        )
        private val SURNAMES = listOf(
            "Abrams", "Abrams", "Ahmad", "Ayala", "Baart", "Bager", "Bakó", "Beckers", "Benoit",
            "Charbonneau", "Clemente", "Dalí", "Ergeshov", "Frank", "Geary", "Gentile", "Georgiev",
            "Giannaki", "Hendrix", "Hepburn", "Hidayat", "Hill", "Hilton", "Honchar", "Hussain",
            "Jansens", "Kartal", "Knez", "Lauwens", "Macháňová", "McGowan", "McKellar", "McNab",
            "Morrish", "Musaev", "Novak", "Pawlitzki", "Picasso", "Protz", "Rana", "Samson",
            "Sappington", "Schäfer", "Souza", "Stankić", "Stauss", "Szymańska", "Tanguy",
            "Thompson", "Van Aarle", "Van der Laar", "Warszawska", "Yılmaz", "Ó Fionnagáin",
            "Čížiková",
        )
        private val NICKNAME_TYPES = listOf(
            ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT,
            ContactsContract.CommonDataKinds.Nickname.TYPE_OTHER_NAME,
            ContactsContract.CommonDataKinds.Nickname.TYPE_MAIDEN_NAME,
            null,
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
            "Kamchatka", "Karachi", "Kathmandu", "Kiev", "Kolkata", "Krasnoyarsk", "Kuala_Lumpur",
            "Kuwait", "La_Paz", "Lima", "Lisbon", "Ljubljana", "London", "Los_Angeles", "Madrid",
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
        )
        private val WEBSITE_TYPES = listOf(
            ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE,
            ContactsContract.CommonDataKinds.Website.TYPE_BLOG,
            ContactsContract.CommonDataKinds.Website.TYPE_PROFILE,
            ContactsContract.CommonDataKinds.Website.TYPE_HOME,
            ContactsContract.CommonDataKinds.Website.TYPE_WORK,
            ContactsContract.CommonDataKinds.Website.TYPE_FTP,
            ContactsContract.CommonDataKinds.Website.TYPE_OTHER,
        )
    }
}
