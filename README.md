# Jetpack Compose Lifecycle

A simple project to understand how the **Android Activity Lifecycle works with Jetpack Compose**.

This project demonstrates:

* Android Activity Lifecycle
* `LifecycleOwner`
* `Lifecycle`
* `LifecycleEventObserver`
* `LocalLifecycleOwner`
* `DisposableEffect`
* Activity lifecycle vs Compose recomposition
* `AlertDialog` and lifecycle behavior
* Observing lifecycle events in Logcat

---

## Table of Contents

* [Android Activity Lifecycle](#android-activity-lifecycle)
* [Activity Lifecycle Flow](#activity-lifecycle-flow)
* [Activity Lifecycle vs Compose](#activity-lifecycle-vs-compose)
* [LifecycleOwner](#lifecycleowner)
* [Lifecycle](#lifecycle)
* [LifecycleEventObserver](#lifecycleeventobserver)
* [LocalLifecycleOwner](#locallifecycleowner)
* [DisposableEffect](#disposableeffect)
* [Complete Lifecycle Observer](#complete-lifecycle-observer)
* [Why Remove the Observer](#why-remove-the-observer)
* [AlertDialog Example](#alertdialog-example)
* [Does AlertDialog Trigger Activity Lifecycle](#does-alertdialog-trigger-activity-lifecycle)
* [What Actually Triggers Lifecycle Events](#what-actually-triggers-lifecycle-events)
* [Logcat Examples](#logcat-examples)
* [Activity Lifecycle vs Recomposition](#activity-lifecycle-vs-recomposition)
* [Lifecycle Events vs Lifecycle States](#lifecycle-events-vs-lifecycle-states)
* [Complete Mental Model](#complete-mental-model)
* [Key Takeaways](#key-takeaways)

---

# Android Activity Lifecycle

Every Android `Activity` has a lifecycle.

The lifecycle describes what happens when an Activity:

* Is created
* Becomes visible
* Becomes interactive
* Loses focus
* Becomes invisible
* Is destroyed

The main lifecycle events are:

```text
ON_CREATE
ON_START
ON_RESUME
ON_PAUSE
ON_STOP
ON_DESTROY
```

---

# Activity Lifecycle Flow

The basic flow is:

```text
        Activity starts
              |
              v
         ON_CREATE
              |
              v
          ON_START
              |
              v
         ON_RESUME
              |
              |
        App is active
              |
              v
          ON_PAUSE
              |
              v
          ON_STOP
              |
              v
        ON_DESTROY
```

When the Activity starts for the first time:

```text
ON_CREATE
    ↓
ON_START
    ↓
ON_RESUME
```

When the user leaves the Activity:

```text
ON_PAUSE
    ↓
ON_STOP
```

When the user comes back:

```text
ON_START
    ↓
ON_RESUME
```

---

# Activity Lifecycle vs Compose

Jetpack Compose does **not replace** the Android Activity lifecycle.

They are different concepts.

```text
Android
   |
   v
Activity
   |
   | Activity Lifecycle
   |
   +--> ON_CREATE
   +--> ON_START
   +--> ON_RESUME
   +--> ON_PAUSE
   +--> ON_STOP
   +--> ON_DESTROY
   |
   v
setContent { }
   |
   v
Compose
   |
   +--> Composition
   |
   +--> Recomposition
   |
   +--> State changes
   |
   +--> UI enters/leaves Composition
```

The Activity lifecycle is controlled by Android.

Compose controls the UI composition and recomposition.

---

# LifecycleOwner

`LifecycleOwner` is an object that owns a lifecycle.

An Activity is a `LifecycleOwner`.

Conceptually:

```text
Activity
    |
    v
LifecycleOwner
    |
    v
Lifecycle
```

Inside a Composable, we can get the current `LifecycleOwner` using:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
```

---

# Lifecycle

The `LifecycleOwner` gives us access to its lifecycle:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current

val lifecycle = lifecycleOwner.lifecycle
```

So the relationship is:

```text
LocalLifecycleOwner.current
            |
            v
      LifecycleOwner
            |
            v
lifecycleOwner.lifecycle
            |
            v
         Lifecycle
```

---

# LifecycleEventObserver

`LifecycleEventObserver` allows us to listen for lifecycle events.

Example:

```kotlin
val observer = LifecycleEventObserver { _, event ->

    when (event) {

        Lifecycle.Event.ON_CREATE -> {
            Log.d("LifecycleObserver", "ON_CREATE")
        }

        Lifecycle.Event.ON_START -> {
            Log.d("LifecycleObserver", "ON_START")
        }

        Lifecycle.Event.ON_RESUME -> {
            Log.d("LifecycleObserver", "ON_RESUME")
        }

        Lifecycle.Event.ON_PAUSE -> {
            Log.d("LifecycleObserver", "ON_PAUSE")
        }

        Lifecycle.Event.ON_STOP -> {
            Log.d("LifecycleObserver", "ON_STOP")
        }

        Lifecycle.Event.ON_DESTROY -> {
            Log.d("LifecycleObserver", "ON_DESTROY")
        }

        Lifecycle.Event.ON_ANY -> {
            Log.d("LifecycleObserver", "ON_ANY")
        }
    }
}
```

This observer receives lifecycle events and allows us to log them.

---

# LocalLifecycleOwner

In Compose, we can get the current lifecycle owner using:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
```

This is useful when a Composable needs to interact with the lifecycle.

For example:

```kotlin
@Composable
fun Screen() {

    val lifecycleOwner = LocalLifecycleOwner.current

}
```

---

# DisposableEffect

We use `DisposableEffect` when we need to perform setup and cleanup related to a Composable.

For example:

```kotlin
DisposableEffect(lifecycleOwner) {

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}
```

Think of it as:

```text
Composable enters Composition
            |
            v
    DisposableEffect
            |
            v
      addObserver()
            |
            |
       Observer active
            |
            v
Composable leaves Composition
            |
            v
        onDispose
            |
            v
     removeObserver()
```

---

# Why Not Add the Observer Directly?

Avoid doing this directly in the body of a Composable:

```kotlin
@Composable
fun Screen() {

    lifecycleOwner.lifecycle.addObserver(observer)

}
```

Compose can recompose.

For example:

```text
Composable
    |
    v
Recomposition
    |
    v
Recomposition
    |
    v
Recomposition
```

If observer registration happens directly in the Composable body, you could end up registering observers repeatedly.

That's why side effects such as lifecycle observer registration should be handled using an appropriate effect API.

---

# Complete Lifecycle Observer

Here is the complete implementation:

```kotlin
@Composable
fun Screen(modifier: Modifier = Modifier) {

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            when (event) {

                Lifecycle.Event.ON_CREATE -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_CREATE"
                    )
                }

                Lifecycle.Event.ON_START -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_START"
                    )
                }

                Lifecycle.Event.ON_RESUME -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_RESUME"
                    )
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_PAUSE"
                    )
                }

                Lifecycle.Event.ON_STOP -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_STOP"
                    )
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_DESTROY"
                    )
                }

                Lifecycle.Event.ON_ANY -> {
                    Log.d(
                        "LifecycleObserver",
                        "ON_ANY"
                    )
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
```

---

# Why Remove the Observer?

We register the observer:

```kotlin
lifecycleOwner.lifecycle.addObserver(observer)
```

Therefore, we should remove it when the effect is disposed:

```kotlin
onDispose {
    lifecycleOwner.lifecycle.removeObserver(observer)
}
```

Think of it like:

```text
addObserver()
     |
     v
Start listening
     |
     v
Lifecycle events
     |
     v
onDispose()
     |
     v
removeObserver()
     |
     v
Stop listening
```

This prevents:

* Duplicate observers
* Unnecessary callbacks
* Observers remaining active when no longer needed
* Unnecessary resource usage

---

# AlertDialog Example

The project also demonstrates a Compose `AlertDialog`.

Example:

```kotlin
@Composable
fun Screen(modifier: Modifier = Modifier) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text("Show Alert Dialog")
        }
    }

    if (showDialog) {

        AlertDialog(

            onDismissRequest = {
                showDialog = false
            },

            title = {
                Text("Confirmation")
            },

            text = {
                Text("Do you want to continue?")
            },

            confirmButton = {

                Button(
                    onClick = {
                        Log.d(
                            "Dialog",
                            "Accepted"
                        )

                        showDialog = false
                    }
                ) {
                    Text("Accept")
                }
            },

            dismissButton = {

                Button(
                    onClick = {
                        Log.d(
                            "Dialog",
                            "Cancelled"
                        )

                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

# Does AlertDialog Trigger Activity Lifecycle?

No.

A normal Compose `AlertDialog` does not normally cause the Activity to go through:

```text
ON_PAUSE
ON_STOP
```

The Activity remains active.

For example:

```text
Activity
    |
    v
ON_CREATE
    |
    v
ON_START
    |
    v
ON_RESUME
    |
    |
    | Open AlertDialog
    |
    v
AlertDialog appears
    |
    |
    | Activity is still RESUMED
    |
    v
Click Accept / Cancel
    |
    v
Dialog disappears
    |
    v
Activity is still RESUMED
```

---

# Why Doesn't AlertDialog Appear in Lifecycle Logs?

The lifecycle observer is observing the **Activity lifecycle**.

It is not observing every UI change.

When this happens:

```kotlin
showDialog = true
```

the Compose state changes.

Compose performs recomposition:

```text
showDialog = true
       |
       v
Recomposition
       |
       v
AlertDialog appears
```

But the Activity is still:

```text
RESUMED
```

Therefore, there is no new Activity lifecycle event.

If you want to log the dialog itself, use a separate log:

```kotlin
Log.d("Dialog", "Dialog opened")
```

---

# What Actually Triggers Lifecycle Events?

Several Android actions can cause Activity lifecycle events.

## Starting the Activity

```text
ON_CREATE
    ↓
ON_START
    ↓
ON_RESUME
```

---

## Pressing Home

Usually:

```text
ON_RESUME
    ↓
ON_PAUSE
    ↓
ON_STOP
```

The Activity is no longer visible.

---

## Returning to the App

Usually:

```text
ON_STOP
    ↓
ON_START
    ↓
ON_RESUME
```

---

## Opening Another Activity

For example:

```text
Activity A
    |
    v
ON_PAUSE
    |
    v
ON_STOP
```

Then Activity B starts:

```text
Activity B
    |
    v
ON_CREATE
    |
    v
ON_START
    |
    v
ON_RESUME
```

The exact behavior can vary depending on how the Activities are launched and whether the previous Activity remains visible.

---

## Pressing Back

If the Activity finishes:

```text
ON_PAUSE
    ↓
ON_STOP
    ↓
ON_DESTROY
```

---

## Screen Turns Off

Depending on the situation, the Activity can move through:

```text
ON_PAUSE
    ↓
ON_STOP
```

---

# Things That Usually Don't Trigger Activity Lifecycle

These normally do not cause Activity lifecycle changes:

```text
Compose recomposition
        ↓
No Activity lifecycle event
```

```text
Changing Compose state
        ↓
No Activity lifecycle event
```

```text
Opening AlertDialog
        ↓
No Activity lifecycle event
```

```text
Scrolling
        ↓
No Activity lifecycle event
```

```text
Changing Text
        ↓
No Activity lifecycle event
```

```text
Showing / hiding a Composable
        ↓
No Activity lifecycle event
```

---

# Logcat Example 1 — Activity Starts

When the Activity is created and becomes active:

![Activity created and resumed](https://raw.githubusercontent.com/androidwithabhishek/my-res/main/compose_lifecycle/img/1.png)

The Logcat output shows the initial lifecycle events.

Typical sequence:

```text
ON_CREATE
ON_START
ON_RESUME
```

This means:

```text
Activity created
      ↓
Activity became visible
      ↓
Activity became interactive
```

---

# Logcat Example 2 — Activity Goes to Background

When the user leaves the Activity, such as pressing the Home button:

![Activity paused and stopped](https://raw.githubusercontent.com/androidwithabhishek/my-res/main/compose_lifecycle/img/2.png)

Typical sequence:

```text
ON_PAUSE
ON_STOP
```

The Activity is no longer in the foreground.

---

# Logcat Example 3 — Returning to the Activity

When the user returns to the app:

![Activity started and resumed again](https://raw.githubusercontent.com/androidwithabhishek/my-res/main/compose_lifecycle/img/3.png)

Typical sequence:

```text
ON_START
ON_RESUME
```

The Activity becomes visible and interactive again.

---

# Logcat Example 4 — Complete Lifecycle Sequence

The complete Logcat sequence can be seen here:

![Complete lifecycle sequence](https://raw.githubusercontent.com/androidwithabhishek/my-res/main/compose_lifecycle/img/4.png)

A typical sequence is:

```text
ON_CREATE
ON_START
ON_RESUME

ON_PAUSE
ON_STOP

ON_START
ON_RESUME
```

This represents:

```text
App starts
    ↓
Activity created
    ↓
Activity visible
    ↓
Activity interactive
    ↓
User leaves app
    ↓
Activity paused
    ↓
Activity stopped
    ↓
User returns
    ↓
Activity started
    ↓
Activity resumed
```

---

# Activity Lifecycle vs Recomposition

This is an important concept in Jetpack Compose.

Suppose we have:

```kotlin
var count by remember {
    mutableStateOf(0)
}
```

And:

```kotlin
Button(
    onClick = {
        count++
    }
) {
    Text("Count: $count")
}
```

When the button is clicked:

```text
count++
    ↓
State changes
    ↓
Recomposition
    ↓
UI updates
```

It does NOT mean:

```text
ON_PAUSE
ON_STOP
ON_START
ON_RESUME
```

The Activity can remain:

```text
ON_RESUME
```

while Compose recomposes multiple times.

---

# Composition vs Activity Lifecycle

Consider:

```text
                 Activity
                    |
                    v
             Activity Lifecycle
                    |
       +------------+------------+
       |            |            |
       v            v            v
  ON_CREATE     ON_RESUME     ON_STOP
                    |
                    |
                    v
                Compose
                    |
          +---------+---------+
          |                   |
          v                   v
     Composition        Recomposition
          |                   |
          v                   v
       UI state           UI update
```

Activity lifecycle and Compose recomposition should not be treated as the same thing.

---

# Lifecycle Events vs Lifecycle States

Android Lifecycle has both **events** and **states**.

## Lifecycle Events

Events describe something that happened:

```text
ON_CREATE
ON_START
ON_RESUME
ON_PAUSE
ON_STOP
ON_DESTROY
```

For example:

```text
ON_RESUME
```

means the Activity has received the resume event.

---

# Lifecycle States

States describe the current lifecycle state:

```text
INITIALIZED
CREATED
STARTED
RESUMED
DESTROYED
```

For example:

```text
ON_RESUME
     ↓
RESUMED
```

And:

```text
ON_PAUSE
     ↓
STARTED
```

A simple way to remember this:

```text
EVENT = Something happened

STATE = Current condition
```

---

# Lifecycle Event vs Lifecycle State

```text
EVENT
  |
  +--> ON_CREATE
  +--> ON_START
  +--> ON_RESUME
  +--> ON_PAUSE
  +--> ON_STOP
  +--> ON_DESTROY


STATE
  |
  +--> INITIALIZED
  +--> CREATED
  +--> STARTED
  +--> RESUMED
  +--> DESTROYED
```

---

# What Is ON_ANY?

`ON_ANY` is a special lifecycle event used by lifecycle observer APIs to represent any lifecycle event.

It is not a normal lifecycle transition that you manually trigger.

Your observer may include:

```kotlin
Lifecycle.Event.ON_ANY -> {
    Log.d(
        "LifecycleObserver",
        "ON_ANY"
    )
}
```

But the important lifecycle transitions to learn first are:

```text
ON_CREATE
ON_START
ON_RESUME
ON_PAUSE
ON_STOP
ON_DESTROY
```

---

# Complete Mental Model

The easiest way to understand the entire system is:

```text
                    ANDROID
                       |
                       v
                   Activity
                       |
                       v
              Activity Lifecycle
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
    ON_CREATE      ON_RESUME       ON_STOP
                       |
                       v
             LocalLifecycleOwner
                       |
                       v
                  Lifecycle
                       |
                       v
            LifecycleEventObserver
                       |
                       v
               DisposableEffect
                       |
                       v
                    Compose
                       |
             +---------+---------+
             |                   |
             v                   v
        Composition        Recomposition
             |                   |
             v                   v
          Compose UI          State changes
             |
             v
        AlertDialog
```

---

# Simple Mental Model

Remember these three things:

### Android controls Activity lifecycle

```text
ON_CREATE
ON_START
ON_RESUME
ON_PAUSE
ON_STOP
ON_DESTROY
```

### Compose controls UI composition

```text
Composition
Recomposition
State changes
```

### Compose can observe Android lifecycle

```text
LocalLifecycleOwner
        ↓
Lifecycle
        ↓
LifecycleEventObserver
        ↓
DisposableEffect
```

---

# Key Takeaways

1. An Android Activity has a lifecycle.

2. The main Activity lifecycle events are:

```text
ON_CREATE
ON_START
ON_RESUME
ON_PAUSE
ON_STOP
ON_DESTROY
```

3. `LifecycleOwner` is an object that owns a lifecycle.

4. `LocalLifecycleOwner.current` gives the current lifecycle owner inside Compose.

5. `lifecycleOwner.lifecycle` gives access to the lifecycle.

6. `LifecycleEventObserver` lets us observe lifecycle events.

7. `DisposableEffect` is useful for registering and cleaning up the observer.

8. Always remove the observer when the effect is disposed:

```kotlin
onDispose {
    lifecycleOwner.lifecycle.removeObserver(observer)
}
```

9. Compose recomposition does not mean the Activity lifecycle changed.

10. A normal Compose `AlertDialog` does not normally trigger:

```text
ON_PAUSE
ON_STOP
```

11. Pressing Home usually causes:

```text
ON_PAUSE
ON_STOP
```

12. Returning to the app usually causes:

```text
ON_START
ON_RESUME
```

13. Activity lifecycle events are controlled by Android.

14. Compose state changes and recomposition are handled by Compose.

---

# Project

GitHub repository:

https://github.com/androidwithabhishek/Compose-Lifecycle

Resource screenshots:

https://github.com/androidwithabhishek/my-res/tree/main/compose_lifecycle/img

---

## License

This project is intended for learning and experimentation with Jetpack Compose and Android lifecycle concepts.
