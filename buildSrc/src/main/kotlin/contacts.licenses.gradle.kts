import contacts.licenses.CopyrightOverride
import contacts.licenses.ExtraNotice
import contacts.licenses.GenerateLicensesTask

tasks.register<GenerateLicensesTask>("generateLicenses") {
    group = "documentation"
    description = "Generates assets/licenses.html from the release runtime classpath."

    output.set(rootProject.file("assets/licenses.html"))

    extraNotices.addAll(
        ExtraNotice(
            name = "AOSP",
            spdxId = "Apache-2.0",
            text = "Copyright (c) 2005-2008, The Android Open Source Project",
        ),
        ExtraNotice(
            name = "GIFLIB",
            spdxId = "MIT",
            text = "The GIFLIB distribution is Copyright (c) 1997  Eric S. Raymond",
        ),
    )
}
