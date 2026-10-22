# Career Connect

An Android app (Kotlin, XML layouts) that helps students find jobs and internships and track their applications.

## Team
- Aiden: Technical Co-Lead, backend (Firebase, security rules, notifications, tests, CI)
- Alice: Frontend Lead (layouts and screens)
- Oratile: QA

## Tech stack
| Layer | Choice |
|---|---|
| App | Android, Kotlin, XML views, minSdk 24 |
| Login | Firebase Authentication (email and password) |
| Database | Cloud Firestore (africa-south1) |
| Public job feed | Retrofit + Gson, calling the Remotive public API |
| Tests | JUnit 4 (unit tests on the status rules) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

Firebase is the backend. There is no separate server to run.

## How the requirements are met
| Requirement | Where |
|---|---|
| Registration and login | `RegisterActivity.kt`, `LoginActivity.kt` (Firebase Auth); profile saved in `users/{uid}` |
| Application tracking (PENDING, INTERVIEW, REJECTED, ARCHIVED) | `ApplicationRepository.kt`, `StatusRules.kt` |
| Notification when status changes | `StatusNotifier.kt` listens to the student's applications and shows a local notification on a real change |
| Timeout auto-archive | A PENDING application older than 14 days is archived by `ApplicationRepository.archiveStale` |
| Employer privacy | `firestore.rules`: an employer can read and update only applications whose `employerId` is their own UID |
| Public job API | `JobApi.kt` (Retrofit) |
| POPIA retention (12 months) | See "Data protection" below |
| Unit tests | `app/src/test/.../StatusRulesTest.kt` |
| CI | `.github/workflows/ci.yml` runs the tests and builds the APK on every push and pull request |

## Data model (Firestore)
- `users/{uid}`: `fullName`, `email`, `role` (`student` or `employer`), `createdAt`
- `jobs/{jobId}`: job details posted by employers
- `applications/{studentId_jobId}`: `studentId`, `studentName`, `studentEmail`, `employerId`, `jobId`, `jobTitle`, `company`, `status`, `appliedAt`, `updatedAt`

The document id `studentId_jobId` means a student can apply to a job only once.

## Security rules
`firestore.rules` is published in the Firebase console. Key points:
- Users can read only their own profile and cannot change their own `role`.
- New accounts can only be created as `student`.
- Only employers can create or edit jobs.
- A student can create an application only for themselves, and only as PENDING.
- Applications are readable only by the student who applied and the employer it was sent to.
- A student can change an application only to archive their own PENDING one.
- Nobody can delete applications from the app.

## Data protection (POPIA)
- Only the personal data needed to run the service is stored: name, email, role and applications.
- Application records are kept for 12 months after their last update (`StatusRules.RETENTION_DAYS`, 365).
- Deletion after 12 months: add a Firestore TTL policy on the `applications` collection group using an `expireAt` timestamp field (set to `updatedAt` plus 365 days). TTL deletion is configured in the Firebase console under Firestore > TTL. If TTL is not available on the project plan, an administrator deletes records past retention manually, using `StatusRules.isPastRetention` as the test.
- A user can ask for their data to be removed by contacting the team; account and `users/{uid}` document are then deleted.

## Public job feed
Remote job listings come from [Remotive](https://remotive.com) (https://remotive.com/api/remote-jobs). Remotive asks that the source is credited and that each job links back to its original posting. The feed is delayed by 24 hours and needs no API key.

## Run it
1. Install Android Studio and open the `android` folder.
2. Make sure `google-services.json` is in `android/app` (download it from Firebase console > Project settings).
3. Press Run on the emulator, or install the signed APK on a phone.

## Run the tests
```
cd android
./gradlew testDebugUnitTest
```
(On Windows use `gradlew.bat testDebugUnitTest`.)

## Test accounts
| Role | Email | Password |
|---|---|---|
| Student | _to be filled in before hand-over_ | _to be filled in_ |
| Employer | _to be filled in before hand-over_ | _to be filled in_ |

## Branches
- `main`: submission branch, only the Technical Co-Lead merges here
- `dev`: integration branch
- `feat/frontend-ui`: Alice's screens
