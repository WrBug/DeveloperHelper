package com.wrbug.developerhelper.commonutil

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Any> Single<T>.runOnIO(): Single<T> {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}