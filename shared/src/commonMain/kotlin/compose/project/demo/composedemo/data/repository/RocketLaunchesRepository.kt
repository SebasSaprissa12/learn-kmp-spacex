package compose.project.demo.composedemo.data.repository

import compose.project.demo.composedemo.data.local.ILocalRocketLaunchesDataSource
import compose.project.demo.composedemo.data.remote.IRemoteRocketLaunchesDataSource
import compose.project.demo.composedemo.domain.entity.RocketLaunch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class RocketLaunchesRepository(
    private val localRocketLaunchesDataSource: ILocalRocketLaunchesDataSource,
    private val remoteRocketLaunchesDataSource: IRemoteRocketLaunchesDataSource,
    private val defaultDispatcher: CoroutineDispatcher
) : IRocketLaunchesRepository {

    override val latestLaunches: Flow<List<RocketLaunch>> = flow {
        emit(remoteRocketLaunchesDataSource.latestLaunches().first())
    }.onEach { launches ->
        localRocketLaunchesDataSource.clearAndCreateLaunches(launches)
    }.catch {
        val cachedLaunches = localRocketLaunchesDataSource.getAllLaunches()
        if (cachedLaunches.isNotEmpty()) {
            emit(cachedLaunches)
        }
    }.flowOn(defaultDispatcher)
}
