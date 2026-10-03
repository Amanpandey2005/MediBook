# MediBook — Hospital Booking System

A hospital appointment booking platform with three separate role areas:
patients book and manage appointments, doctors manage their schedules, and
admins oversee users, doctors and feedback. Built as a team project.

Server-rendered Spring Boot application using Thymeleaf, with Spring
Security handling access control across the three roles.

## Stack

| Layer | Choice |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + `thymeleaf-extras-springsecurity6` |
| Persistence | Spring Data JPA over H2 (file-backed) |
| Views | Thymeleaf |
| Mail | Spring Boot Mail (SMTP) for password resets |
| AI | Google Gemini via its OpenAI-compatible endpoint (OkHttp) |

## Features

- Role-based access for `PATIENT`, `DOCTOR` and `ADMIN`, enforced by Spring Security.
- Appointment booking against doctors, with schedule management on the doctor side.
- Medication and feedback records modelled through JPA.
- Password reset over email using single-use, expiring tokens (`PasswordResetToken`).
- An AI recommendation feature that suggests the right service for a patient's
  described symptoms, calling Gemini through `GeminiService`.

## Running it locally

Requires **Java 17+** and **Maven**.

```bash
# 1. Provide configuration (see .env.example for the full list)
export GEMINI_API_KEY=your-key        # optional; AI suggestions are skipped without it
export MAIL_USERNAME=your@gmail.com   # optional; password-reset mail is skipped without it
export MAIL_PASSWORD=your-app-password

# 2. Run
mvn spring-boot:run
```

Then open http://localhost:8080.

The H2 database is created on first run under `data/`, which is gitignored.
The H2 console is available at `/h2-console`.

### Seeded accounts

`DataInitializer` creates demo data on first run:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@medbook.com` | `admin123` |
| Doctor | `dr.smith@medbook.com` (and nine more `dr.*@medbook.com`) | `password123` |

These are local development accounts only.

## Configuration

No secrets are committed. Every credential is read from the environment,
with the keys listed in [`.env.example`](.env.example):

| Variable | Purpose |
| --- | --- |
| `MAIL_USERNAME` | SMTP account used as the sender for password-reset mail |
| `MAIL_PASSWORD` | Google **app password**, not the account password |
| `GEMINI_API_KEY` | Google AI Studio key for the recommendation feature |
| `DB_PASSWORD` | Local H2 datasource password |

## Project layout

```
src/main/java/com/medbook/
├── config/       # Security config, user details service, data seeding
├── controller/   # Auth, patient, doctor, admin, home, contact
├── entity/       # User, Doctor, Appointment, Medication, Feedback, PasswordResetToken
├── repository/   # Spring Data JPA repositories
└── service/      # Booking, users, mail, password reset, Gemini integration
src/main/resources/
├── templates/    # Thymeleaf views
└── static/       # Images and assets
```

## Known limitations

- Deployment is local-only; there is no hosted instance.
- Seeding registers the demo doctors one at a time and tries to send each a
  welcome email, so startup is slow and logs a stack trace per doctor when no
  SMTP credentials are configured. Seeding still completes and the app starts.
- Some authentication paths log diagnostic detail to stdout and should be
  moved to a logger with appropriate levels.
