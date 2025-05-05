import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.storage.Store
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.renderComposable
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.test.storage.TestStorage

@Markup
@Suppress("FunctionName")
fun <T> TestApplication(lens: Lens<Application, T>, component:@Composable (Storage<T>)->Unit) = renderComposable("root") {
    //installSerializers()

    Store({ TestStorage()  }) {
        component(this * lens)
    }
}