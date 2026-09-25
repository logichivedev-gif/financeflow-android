# Fix Corrupted XML Resource Files

Many XML resource files in the project have truncated namespace declarations (e.g., `xmlns:android="http:`), which causes build failures. This plan aims to identify and fix all such occurrences.

## Proposed Changes

### Android Resources

The following files will be updated to restore the correct XML namespace URLs:

#### [MODIFY] [ic_launcher_foreground.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/ic_launcher_foreground.xml)
#### [MODIFY] [widget_background.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_background.xml)
#### [MODIFY] [widget_button_background.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_button_background.xml)
#### [MODIFY] [widget_preview_actions_img.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_preview_actions_img.xml)
#### [MODIFY] [widget_preview_balance_img.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_preview_balance_img.xml)
#### [MODIFY] [widget_preview_transactions_img.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_preview_transactions_img.xml)
#### [MODIFY] [widget_status_danger.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_status_danger.xml)
#### [MODIFY] [widget_status_safe.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_status_safe.xml)
#### [MODIFY] [widget_status_warning.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/drawable/widget_status_warning.xml)
#### [MODIFY] [widget_actions_layout.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_actions_layout.xml)
#### [MODIFY] [widget_balance_layout.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_balance_layout.xml)
#### [MODIFY] [widget_preview_actions.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_preview_actions.xml)
#### [MODIFY] [widget_preview_balance.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_preview_balance.xml)
#### [MODIFY] [widget_preview_transactions.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_preview_transactions.xml)
#### [MODIFY] [widget_transactions_layout.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/layout/widget_transactions_layout.xml)
#### [MODIFY] [ic_launcher.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
#### [MODIFY] [ic_launcher_round.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
#### [MODIFY] [file_paths.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/xml/file_paths.xml)
#### [MODIFY] [widget_actions_info.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/xml/widget_actions_info.xml)
#### [MODIFY] [widget_balance_info.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/xml/widget_balance_info.xml)
#### [MODIFY] [widget_transactions_info.xml](file:///C:/Users/Abel/Documents/Proyectos Android/financeflowfinal/app/src/main/res/xml/widget_transactions_info.xml)

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that all resources parse correctly and the build succeeds.
