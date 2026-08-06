package com.android.contacts.domain.debug.usecase

import android.graphics.Bitmap
import android.graphics.Color
import android.provider.ContactsContract
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.android.contacts.domain.debug.model.TestContact
import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

internal fun interface GenerateTestContact {
    operator fun invoke(): TestContact
}

internal class GenerateTestContactImpl @Inject constructor() : GenerateTestContact {
    override fun invoke(): TestContact {
        val givenName = NAMES.random()
        val middleName = if (shouldAddExtraProperty()) NAMES.random() else null
        val familyName = if (shouldAddExtraProperty()) SURNAMES.random() else null
        val displayName = if (shouldAddExtraProperty()) {
            listOfNotNull(
                familyName,
                middleName,
                familyName,
            ).joinToString(" ")
        } else {
            null
        }

        return TestContact(
            phones = (1..3).map { randomPhone() },
            givenName = givenName,
            middleName = middleName,
            familyName = familyName,
            displayName = displayName,
            nickname = if (shouldAddExtraProperty()) randomNickname() else null,
            emails = (0..2).map { randomEmail(givenName) },
            city = if (shouldAddExtraProperty()) CITIES.random() else null,
            country = if (shouldAddExtraProperty()) COUNTRIES.random() else null,
            organization = if (shouldAddExtraProperty()) SURNAMES.random() else null,
            relation = if (shouldAddExtraProperty()) RELATIONS.random() else null,
            website = if (shouldAddExtraProperty()) randomWebsite(givenName) else null,
            photo = if (shouldAddExtraProperty()) randomPhoto() else null,
        )
    }

    private fun shouldAddExtraProperty(): Boolean {
        return Random.nextBoolean()
    }

    private fun randomPhone(): TestContact.ValueWithType {
        val number = TestContact.PHONE_PREFIX +
            Random.nextInt(999_999).toString().padStart(6, '0')
        val type = PHONE_TYPES.random()
        return TestContact.ValueWithType(number, type)
    }

    private fun randomNickname(): TestContact.ValueWithType {
        return TestContact.ValueWithType(
            (NAMES + SURNAMES).random(),
            NICKNAME_TYPES.random(),
        )
    }

    private fun randomEmail(name: String): TestContact.ValueWithType {
        return TestContact.ValueWithType(
            "${name.lowercase()}@example.org",
            EMAIL_TYPES.random(),
        )
    }

    private fun randomWebsite(name: String): TestContact.ValueWithType {
        return TestContact.ValueWithType(
            "https://${name.lowercase()}.me",
            WEBSITE_TYPES.random(),
        )
    }

    private fun randomPhoto(): ByteArray? {
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
        return bos.toByteArray()
    }

    private fun randomColor(): Int {
        return Color.rgb(
            Random.nextInt(COLOR_RANGE),
            Random.nextInt(COLOR_RANGE),
            Random.nextInt(COLOR_RANGE),
        )
    }

    companion object {
        private const val PHOTO_SIZE = 16
        private val COLOR_RANGE = 0..255
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
        private val COUNTRIES = Locale.availableLocales().map { it.displayCountry }.toList()
        private val RELATIONS = listOf(
            ContactsContract.CommonDataKinds.Relation.TYPE_ASSISTANT,
            ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_CHILD,
            ContactsContract.CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER,
            ContactsContract.CommonDataKinds.Relation.TYPE_FATHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND,
            ContactsContract.CommonDataKinds.Relation.TYPE_MANAGER,
            ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_PARENT,
            ContactsContract.CommonDataKinds.Relation.TYPE_PARTNER,
            ContactsContract.CommonDataKinds.Relation.TYPE_REFERRED_BY,
            ContactsContract.CommonDataKinds.Relation.TYPE_RELATIVE,
            ContactsContract.CommonDataKinds.Relation.TYPE_SISTER,
            ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE,
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
