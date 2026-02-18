package compose.project.demo.composedemo.data.repository

// shared/src/commonTest/kotlin/compose/project/demo/composedemo/data/repository/RocketLaunchesRepositoryTest.kt
class RocketLaunchesRepositoryTest {

    private val localDataSource = mock<ILocalRocketLaunchesDataSource>()
    private val remoteDataSource = mock<IRemoteRocketLaunchesDataSource>()

    @Test
    fun `latestLaunches should fetch from remote and save to local`() = runTest {
        // Arrange
        val remoteLaunches =
            listOf(
                RocketLaunch(
                    flightNumber = 1,
                    missionName = "Falcon 1",
                    launchDateUTC = "2006-03-24T22:30:00.000Z",
                    details = null,
                    launchSuccess = false,
                    links = Links(Patch(null, null), null),
                )
            )

        every { remoteDataSource.latestLaunches() } returns flowOf(remoteLaunches)
        every { localDataSource.clearAndCreateLaunches(remoteLaunches) } returns Unit

        val repository =
            RocketLaunchesRepository(
                localRocketLaunchesDataSource = localDataSource,
                remoteRocketLaunchesDataSource = remoteDataSource,
                defaultDispatcher = Dispatchers.Unconfined,
            )

        // Act
        val result = repository.latestLaunches.first()

        // Assert
        assertEquals(remoteLaunches, result)
        verify { localDataSource.clearAndCreateLaunches(remoteLaunches) }
    }

    @Test
    fun `latestLaunches should return cached data when remote fails`() = runTest {
        // Arrange
        val cachedLaunches =
            listOf(
                RocketLaunch(
                    flightNumber = 2,
                    missionName = "Cached Mission",
                    launchDateUTC = "2024-01-01T00:00:00Z",
                    details = null,
                    launchSuccess = true,
                    links = Links(Patch(null, null), null),
                )
            )
        every { remoteDataSource.latestLaunches() } returns flow { throw Exception("Remote error") }
        every { localDataSource.getAllLaunches() } returns cachedLaunches

        val repository =
            RocketLaunchesRepository(
                localRocketLaunchesDataSource = localDataSource,
                remoteRocketLaunchesDataSource = remoteDataSource,
                defaultDispatcher = Dispatchers.Unconfined,
            )

        // Act
        val result = repository.latestLaunches.first()

        // Assert
        assertEquals(cachedLaunches, result)
        verify { localDataSource.getAllLaunches() }
    }
}