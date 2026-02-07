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
        // Repository
        container.register(SchedulerRepositoryProtocol.self) { _ in
            SchedulerRepository()
        }.inObjectScope(.container)

        // Shared State (singleton so all ViewModels share the same data)
        container.register(SharedState.self) { resolver in
            SharedState(repository: resolver.resolve(SchedulerRepositoryProtocol.self)!)
        }.inObjectScope(.container)

        // ViewModels
        container.register(CalendarView.ViewModel.self) { resolver in
            CalendarView.ViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(SchedulerRepositoryProtocol.self)!
            )
        }

        container.register(AvailabilityView.ViewModel.self) { resolver in
            AvailabilityView.ViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(SchedulerRepositoryProtocol.self)!
            )
        }

        container.register(ScheduleView.ViewModel.self) { resolver in
            ScheduleView.ViewModel(
                sharedState: resolver.resolve(SharedState.self)!,
                repository: resolver.resolve(SchedulerRepositoryProtocol.self)!
            )
        }

        container.register(SettingsView.ViewModel.self) { resolver in
            SettingsView.ViewModel(
                sharedState: resolver.resolve(SharedState.self)!
            )
        }
    }

    func resolve<T>(_ type: T.Type) -> T {
        container.resolve(type)!
    }
}
