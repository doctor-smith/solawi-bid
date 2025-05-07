# Frontend Notes

## Data Management
All relevant data is kept in the ```data class Application```. The class is wrapped into a ```Storage```:

```kotlin
Storage<Application> {
    val read: (Unit)->Application
    val write: (Application)->(Unit)->Unit
}
```
Under the hood, data is managed by kotlin compose state.
The signatures of the read/write functions of Storage become more convenient if we take into account that a Storage<Data> 
is actually a Lens<Unit, Data>.
The signatures of the Lens functions are
```kotlin
Lens<W, P> {
    val get: (W)->P
    val set: (P)->(W)->W    
}
```
It is well known that lenses can be multiplied in some sense.
```kotlin
Lens<W, P> * Lens<P, Q> : Lens<W, Q>
```
So we are able to multiply Lenses with Storage and obtain Storage:
```kotlin
Storage<W> * Lens<W, P> : Storage<P>
```

We can even go further and mutliply Storage / Lenses with Readers and Writers:

Readers:
```kotlin
typealias Reader<E, T> = (E)->T

Storage<W> * Reader<W, P> : Reader<Unit, P>
```
A ```Reader<Unit, T>``` is also called a ```Source<T>```.

Writers:
```kotlin
typealias Writer<W, P> = (P)->(W)->W

Storage<W> * Writer<W, P> = Writer<Unit, P>
```

A ```Writer<Unit, T>``` is also called a ```Dispatcher<T>```.








## The Api Model
It is convenient to put an api model into the shared project ```solawi-bid-api-data```

```kotlin
import kotlin.reflect.KClass

data class Api(
    val endPoints: HashMap<KClass<EndPoint<*,*>>, EndPoint<*,*>>
)

sealed class EndPoint<in S, out T>(open val url: String) {
    data class Get<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Post<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Put<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Patch<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Delete<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Head<in S, out T>(override val url: String) : EndPoint<S, T>(url)
}


```


## Structure of the Request Action

### Sketch
* A request takes some input S and yields a Result<T>
* Use Result<(X)->Y>.apply() to write the result to Storage

Api calls have the following structure
```kotlin

Read * Call * Dispatch

```
where * is the state monad multiplication and
```kotlin
typealias Storage = Storage<Application>
// Read input from storage
val Read: State<Storage, S> = TODO()

// perform Api call
val Call: KlState<Storage, S, Result<T>> = TODO()

// write result to storage
val Dispatch: KlState<Storage, Result<T>, Unit /* Result<T> */>
```

### 1. Read 
Tasks: 
* Read data which is necessary to perform the request, i.e. Login credentials
* Provide standard headers, tokens etc
* Provide a configured client 

### 2. Call

Wish:
* Just write down the action and submit it to the surrounding process

### 3. Dispatch

the third is tricky, because we have to get 'under the result'.
We can do that using the applicative structure of the Result. The DispatchState below then plays the role of Dispatch from above.
```kotlin
data class Whole(val number: Int, val name: String, val error: String? = null)

var whole = Whole(0,"")

val storage = Storage<Whole> (
    read = {whole},
    write = {w -> whole = w}
)
val result = Result.Success(name)
val successWriter: Writer<Whole, String> = { n -> { w-> w.copy(name= n)} }
val failureWriter: Writer<Whole, String> = { e -> { w-> w.copy(error = e)} }

val success = {storage: Storage<Whole> -> Result.Return((storage * successWriter).dispatch())}
val failure = {storage: Storage<Whole> -> Result.Return((storage * failureWriter).dispatch())}

/**
 * 
 */
val DispatchState = KlState<Storage<Whole>,Result<String>, Result<Unit>> {
    r -> State { storage ->
        when(r) {
            is Result.Success -> success(storage).apply() on r
            is Result.Failure.Message -> failure(storage).apply() on Result.Return(r.value)
            is Result.Failure.Exception -> failure(storage).apply() on Result.Return(r.value.message?: "")
        } x storage
    }
}
val u = DispatchState(result) runOn storage

```
