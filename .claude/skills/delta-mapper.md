# RawContactDelta Mapper

Build RawContactDeltaList from Compose UiState for ContactSaveService.

## When to Use

When modifying the RawContactDeltaMapper or adding new field types to the save flow.

## Core Concept

For a NEW contact, the delta is in "insert mode":
- `ValuesDelta.fromAfter(contentValues)` — creates an insert delta (mBefore=null, mAfter=contentValues)
- Assigns a **negative temp ID** via `sNextInsertId--`
- Photos reference the temp ID in the `EXTRA_UPDATED_PHOTOS` bundle

## Creating a Delta for Each Field Type

```kotlin
// 1. Create raw contact with account
val rawContact = RawContact().apply {
    if (account != null) setAccount(account) else setAccountToLocal()
}
val delta = RawContactDelta(ValuesDelta.fromAfter(rawContact.values))
val tempId = delta.values.id  // negative temp ID

// 2. Add field entries — each is a ValuesDelta with MIMETYPE set
private inline fun contentValues(mimeType: String, block: ContentValues.() -> Unit) =
    ContentValues().apply { put(Data.MIMETYPE, mimeType); block() }

// Name
delta.addEntry(ValuesDelta.fromAfter(contentValues(StructuredName.CONTENT_ITEM_TYPE) {
    put(StructuredName.GIVEN_NAME, firstName)
    put(StructuredName.FAMILY_NAME, lastName)
    put(StructuredName.PREFIX, prefix)
    put(StructuredName.MIDDLE_NAME, middleName)
    put(StructuredName.SUFFIX, suffix)
}))

// Phone (repeatable)
delta.addEntry(ValuesDelta.fromAfter(contentValues(Phone.CONTENT_ITEM_TYPE) {
    put(Phone.NUMBER, number)
    put(Phone.TYPE, type.rawValue)
    if (type is PhoneType.Custom) put(Phone.LABEL, type.label)
}))
```

## Complete Column Reference

| MIME Type | Columns |
|-----------|---------|
| `StructuredName` | `GIVEN_NAME`, `FAMILY_NAME`, `PREFIX`, `MIDDLE_NAME`, `SUFFIX` |
| `Phone` | `NUMBER`, `TYPE`, `LABEL` (if custom) |
| `Email` | `DATA` (= address), `TYPE`, `LABEL` |
| `StructuredPostal` | `STREET`, `CITY`, `REGION`, `POSTCODE`, `COUNTRY`, `TYPE` |
| `Organization` | `COMPANY`, `TITLE` |
| `Note` | `NOTE` |
| `Website` | `URL`, `TYPE` |
| `Event` | `START_DATE`, `TYPE` |
| `Relation` | `NAME`, `TYPE` |
| `Im` | `DATA`, `PROTOCOL` |
| `Nickname` | `NAME` |
| `SipAddress` | `SIP_ADDRESS` |
| `GroupMembership` | `GROUP_ROW_ID` |

## Custom Type Labels

When type = TYPE_CUSTOM, you MUST set BOTH columns:
```kotlin
put(Phone.TYPE, Phone.TYPE_CUSTOM)
put(Phone.LABEL, "Custom Label Here")
```
If you set TYPE_CUSTOM without LABEL, the label displays as empty.

## Photos

Photos are NOT in the delta. They go in a separate Bundle:
```kotlin
val updatedPhotos = Bundle()
photoUri?.let { updatedPhotos.putParcelable(tempId.toString(), it) }
```

ContactSaveService resolves negative temp IDs to real IDs after insert.

## Empty Field Handling

- `RawContactModifier.trimEmpty()` runs INSIDE `ContactSaveService.saveContact()` before building diff
- Empty entries get `markDeleted()` — never persisted
- The mapper should SKIP blank entries to keep `hasPendingChanges()` accurate
- If ALL entries are empty, the entire delta is deleted = no-op save

## createSaveContactIntent Signature

```kotlin
ContactSaveService.createSaveContactIntent(
    context: Context,
    state: RawContactDeltaList,
    saveModeExtraKey: String,      // key name for callback
    saveMode: Int,                  // SaveMode.CLOSE
    isProfile: Boolean,             // false
    callbackActivity: Class<*>,     // ContactCreationActivity::class.java
    callbackAction: String,         // your custom action string
    updatedPhotos: Bundle,          // tempId(String) → Uri
    joinContactIdExtraKey: String?, // null for new
    joinContactId: Long?,           // null for new
)
```

## Testing the Mapper

Highest priority tests. Verify:
1. Each of 13 field types maps to correct MIME type + columns
2. Empty fields are excluded
3. Custom type labels set both TYPE and LABEL
4. Photo URI in updatedPhotos bundle with correct temp ID key
5. Account set correctly (or local when null)
6. Multiple repeatable fields produce multiple ValuesDelta entries
