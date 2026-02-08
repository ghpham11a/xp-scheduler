//
//  SchedulerApp.swift
//  Scheduler
//
//  Created by Anthony Pham on 1/27/26.
//

import SwiftUI

@main
struct SchedulerApp: App {
    private let container = DependencyContainer.shared

    var body: some Scene {
        WindowGroup {
            ContentView(container: container)
        }
    }
}
