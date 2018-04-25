package com.financial.quantgroup.v2.bus

/**
 * Created by cz on 2017/6/29.
 */

import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import java.util.*

object RxBus {
    private val bus = SerializedSubject(PublishSubject.create<Any>())
    private val subscribeItems: HashMap<String, MutableList<Subscription>> = HashMap()

    /**
     * 发送事件
     * @param o
     */
    fun post(o: Any) {
        bus.onNext(o)
    }

    fun <T> subscribe(obj: Any, eventType: Class<T>, action1: (T) -> Unit) {
        subscribeInner(System.identityHashCode(obj).toString(), eventType, action1, null)
    }

    fun <T> subscribe(obj: Any, eventType: Class<T>, action1: (T) -> Unit, action2: ((Throwable) -> Unit)?) {
        subscribeInner(System.identityHashCode(obj).toString(), eventType, action1, action2)
    }


    /**
     * 根据class event type观察事件
     * @param <T>
     * *
     * @param eventType
     * *
     * @return
    </T> */
    private fun <T> subscribeInner(subscribeTag: String, eventType: Class<T>, action1: (T) -> Unit, action2: ((Throwable) -> Unit)?) {
        var items: MutableList<Subscription>? = subscribeItems[subscribeTag]
        if (null == items) {
            items = ArrayList<Subscription>()
            subscribeItems.put(subscribeTag, items)
        }
        val observable = bus.filter { eventType.isInstance(it) }.cast(eventType).subscribeOn(AndroidSchedulers.mainThread())
        if (null != action1) {
            items.add(observable.subscribe({ action1.invoke(it) }, { action2?.invoke(it) }))
        }
    }

    fun unSubscribeAll() {
        for ((_, items) in subscribeItems) {
            if (null != items) {
                for (i in items.indices) {
                    val subscription = items[i]
                    if (null != subscription && !subscription.isUnsubscribed) {
                        subscription.unsubscribe()
                    }
                }
                items.clear()
            }
        }
    }

    fun unSubscribeItems(obj: Any) {
        unSubscribeItemsInner(System.identityHashCode(obj).toString())
    }

    private fun unSubscribeItemsInner(subscribeTag: String) {
        val items = RxBus.subscribeItems[subscribeTag]
        if (null != items) {
            items.indices
                    .map { items[it] }
                    .filter { null != it && !it.isUnsubscribed }
                    .forEach { it.unsubscribe() }
            items.clear()
        }
    }
}

/**
 * 订阅事件
 */
inline fun <reified T, E> T.subscribe(eventType: Class<E>, noinline action: (E) -> Unit) {
    RxBus.subscribe(this as Any, eventType, action, null)
}

/**
 * 取消订阅,自动回调的对象,无须用户调用
 */
inline fun <reified T> T.unSubscribeItems() {
    RxBus.unSubscribeItems(this as Any)
}