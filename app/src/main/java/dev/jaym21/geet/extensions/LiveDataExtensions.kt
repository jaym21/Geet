package dev.jaym21.geet.extensions

import androidx.lifecycle.*

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onEmission: (T) -> Unit) {
    return observe(owner, Observer<T> {
        if (it != null) {
            onEmission(it)
        }
    })
}

fun <X, Y> LiveData<X>.map(mapper: (X) -> Y) =
    Transformations.map(this, mapper)

typealias LiveDataFilter<T> = (T) -> Boolean

class FilterLiveData<T>(
    source1: LiveData<T>,
    private val filter: LiveDataFilter<T>
) : MediatorLiveData<T>() {

    init {
        super.addSource(source1) {
            if (filter(it)) {
                value = it
            }
        }
    }

    override fun <S : Any?> addSource(
        source: LiveData<S>,
        onChanged: Observer<in S>
    ) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}

fun <T> LiveData<T>.filter(filter: LiveDataFilter<T>): MediatorLiveData<T> = FilterLiveData(this, filter)
