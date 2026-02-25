import Foundation
import Swinject

final class DependencyContainer {
    static let shared = DependencyContainer()

    let container: Container

    private init() {
        container = Container()
        
        registerDependencies()
    }

    private func registerDependencies() {
        // Networking
        container.register(Networking.self) { _ in
            NetworkService()
        }.inObjectScope(.container)

        // Connectivity
        container.register(ConnectivityMonitor.self) { _ in
            ConnectivityMonitor()
        }.inObjectScope(.container)

        // Navigation
        container.register(RouteManager.self) { _ in
            RouteManager()
        }.inObjectScope(.container)

        // Repositories
        container.register(UsersRepo.self) { resolver in
            UsersRepository(networking: resolver.resolve(Networking.self)!)
        }.inObjectScope(.container)

        container.register(AvailabilitiesRepo.self) { resolver in
            AvailabilitiesRepository(networking: resolver.resolve(Networking.self)!)
        }.inObjectScope(.container)

        container.register(MeetingsRepo.self) { resolver in
            MeetingsRepository(networking: resolver.resolve(Networking.self)!)
        }.inObjectScope(.container)

        // Shared State
        container.register(SharedState.self) { resolver in
            SharedState(
                usersRepo: resolver.resolve(UsersRepo.self)!,
                availabilitiesRepo: resolver.resolve(AvailabilitiesRepo.self)!,
                meetingsRepo: resolver.resolve(MeetingsRepo.self)!
            )
        }.inObjectScope(.container)

        // ViewModels
        container.register(CalendarViewModel.self) { resolver in
            CalendarViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(MeetingsRepo.self)!
            )
        }

        container.register(AvailabilityViewModel.self) { resolver in
            AvailabilityViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(AvailabilitiesRepo.self)!
            )
        }

        container.register(ScheduleViewModel.self) { resolver in
            ScheduleViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(MeetingsRepo.self)!
            )
        }

        container.register(SettingsViewModel.self) { resolver in
            SettingsViewModel(
                sharedState: resolver.resolve(SharedState.self)!
            )
        }
    }

    func resolve<T>(_ type: T.Type) -> T {
        container.resolve(type)!
    }
}
