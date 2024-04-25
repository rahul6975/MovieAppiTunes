import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.features.viewModel.MovieViewModel
import com.rahul.movieappitunes.network.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.*


@ExperimentalCoroutinesApi
class MovieViewModelTest {


    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val testScope = TestCoroutineScope(testDispatcher)

    private lateinit var viewModel: MovieViewModel

    @Mock
    private lateinit var movieRepository: MovieRepository

    @Mock
    private lateinit var mockObserver: Observer<List<MovieEntity>>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        Dispatchers.setMain(testDispatcher)

        viewModel = MovieViewModel()
        viewModel.searchedMovies.observeForever(mockObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun `test search movies`() {
        // Given
        val keyword = "Test"
        val movies = listOf(
            MovieEntity(
                id = "1",
                name = "Test Movie",
                price = 9.99,
                currency = "USD",
                shortDesc = "Short description",
                longDesc = "Long description",
                releaseDate = Date(), // You need to provide a Date object here
                genre = "Action",
                actor = "John Doe",
                preview = null, // Provide a Uri object or null
                image = null, // Provide a Uri object or null
                favorite = false // Provide a Boolean value
            )
        )


        // When
        viewModel.search(keyword)

        // Then
        Mockito.verify(movieRepository).getMovies(keyword)
        Mockito.verify(mockObserver).onChanged(movies)
    }

}
