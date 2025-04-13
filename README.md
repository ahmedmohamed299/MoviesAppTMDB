# MovieApp Mazaady

A modern Android application for browsing and managing favorite movies using TMDB API.

## Architecture

The app follows Clean Architecture principles with MVVM pattern, using the following key components:

- **Data Layer**
  - Repository pattern for data abstraction
  - Room Database for local storage
  - Retrofit for API calls
  - Paging 3 for efficient data loading
  - Repository interfaces
  - Data models


- **Presentation Layer**
  - MVVM architecture
  - ViewModels with StateFlow
  - Navigation component

## Setup Instructions

1. Clone the repository
2. Add your TMDB API key to `local.properties`:
   ```
   TMDB_API_KEY=your_api_key_here
   ```
3. Sync project with Gradle files
4. Run the app on an emulator or physical device

## Key Decisions & Trade-offs

### Architecture Choices
- **MVVM with Clean Architecture**: Chosen for separation of concerns and testability
- **Single Activity Architecture**: Using Navigation Component for better state management
- **Repository Pattern**: Abstracts data sources and provides a single source of truth

### Technical Decisions
- **Paging 3**: For efficient loading of movie data
- **Room Database**: For offline support and caching
- **Hilt**: For dependency injection

### Trade-offs
- **Offline First**: Prioritizes local data storage, which may lead to slightly stale data
- **Paging Complexity**: Added complexity in data layer for better user experience
- **Minimal Dependencies**: Keeps third-party libraries to a minimum for maintainability

## Testing Strategy
- Unit tests for business logic
- Fake implementations for testing
- Test dispatchers for coroutine testing
- Mockito for dependency mocking
