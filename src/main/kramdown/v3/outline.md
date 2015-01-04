- Workspace setup guide for both Eclipse and IntelliJ
- Ex 1: StaticInt.java : Ice breaking, eye opening. Making the readers see that Class Reloading is possible, and Classes are not "GLOBAL" and SINGLETON as most of them may think.
- Ex 2: ReloadingContinuously.java : Demonstrate the dynamic class loader to pick up class changes on the fly. This is what every one looking for.
- Ex 3: ContextReloading.java : Give them more confidence, reloading the whole application context. Giving advice on isolating every things that are reloaded.
- Ex 4: KeepConnectionPool.java : Visualize the separation line between what is persisted and what is reloaded, and the connections between the 2 worlds.
- Ex 5: WebApp.java : Nearest to real world application.
Things that I did setup with class reloading