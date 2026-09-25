# Fix XML Resource Syntax Errors

The project is currently failing to build due to multiple XML resource files having truncated or malformed namespace declarations (specifically `xmlns:android` and `xmlns:aapt`). This was likely caused by an incomplete search-and-replace operation.

## Proposed Changes

I will fix the following files by restoring the correct namespace URLs:
- `xmlns:android="http://schemas.android.com/apk/res/android"`
- `xmlns:aapt="http://schemas.android.com/aapt"`

### Affected Files

#### [MODIFY] Multiple Resource Files
The following files in `app/src/main/res/` will be updated:
- `drawable/ic_launcher_background.xml`
- `drawable/ic_launcher_foreground.xml`
- `drawable/widget_background.xml`
- `drawable/widget_button_background.xml`
- `drawable/widget_preview_actions_img.xml`
- `drawable/widget_preview_balance_img.xml`
- `drawable/widget_preview_transactions_img.xml`
- `drawable/widget_status_danger.xml`
- `drawable/widget_status_safe.xml`
- `drawable/widget_status_warning.xml`
- `layout/widget_actions_layout.xml`
- `layout/widget_balance_layout.xml`
- `layout/widget_preview_actions.xml`
- `layout/widget_preview_balance.xml`
- `layout/widget_preview_transactions.xml`
- `layout/widget_transactions_layout.xml`
- `mipmap-anydpi-v26/ic_launcher.xml`
- `mipmap-anydpi-v26/ic_launcher_round.xml`
- `xml/file_paths.xml`
- `xml/widget_actions_info.xml`
- `xml/widget_balance_info.xml`
- `xml/widget_transactions_info.xml`

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure all resources compile and the project builds successfully.
