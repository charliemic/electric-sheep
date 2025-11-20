package com.electricsheep.app.ui.data

/**
 * Collection of witty subtitles for the landing page.
 * Inspired by Oscar Wilde's style of witticisms and clever observations.
 * All quotes are kept to a suitable length for subtitle display.
 */
object LandingSubtitles {
    val quotes = listOf(
        "Personal Utilities",
        "Be yourself; everyone else is already taken.",
        "We are all in the gutter, but some of us are looking at the stars.",
        "Experience is simply the name we give our mistakes.",
        "To live is the rarest thing in the world. Most people just exist.",
        "Always forgive your enemies; nothing annoys them so much.",
        "I can resist everything except temptation.",
        "The only way to get rid of a temptation is to yield to it.",
        "A man's face is his autobiography. A woman's face is her work of fiction.",
        "I am so clever that sometimes I don't understand a single word of what I am saying.",
        "Fashion is a form of ugliness so intolerable that we have to alter it every six months.",
        "The truth is rarely pure and never simple.",
        "We live in an age when unnecessary things are our only necessities.",
        "Some cause happiness wherever they go; others whenever they go.",
        "I have the simplest tastes. I am always satisfied with the best.",
        "A little sincerity is a dangerous thing, and a great deal of it is absolutely fatal.",
        "The only thing worse than being talked about is not being talked about.",
        "I think that God, in creating man, somewhat overestimated his ability.",
        "Man is least himself when he talks in his own person. Give him a mask, and he will tell you the truth.",
        "The world is a stage, but the play is badly cast.",
        "Nothing that is worth knowing can be taught.",
        "Every saint has a past, and every sinner has a future.",
        "I don't want to go to heaven. None of my friends are there.",
        "A good friend will always stab you in the front.",
        "The old believe everything, the middle-aged suspect everything, the young know everything.",
        "I am not young enough to know everything.",
        "Work is the curse of the drinking classes.",
        "I have nothing to declare except my genius.",
        "A cynic is a man who knows the price of everything and the value of nothing.",
        "I can believe anything, provided that it is quite incredible.",
        "Life is far too important a thing ever to talk seriously about.",
        "I never travel without my diary. One should always have something sensational to read in the train.",
        "The difference between journalism and literature is that journalism is unreadable and literature is not read.",
        "I like persons better than principles, and I like persons with no principles better than anything else in the world.",
        "One should always be in love. That is the reason one should never marry.",
        "The basis of optimism is sheer terror.",
        "A thing is not necessarily true because a man dies for it.",
        "I put all my genius into my life; I put only my talent into my works.",
        "The public is wonderfully tolerant. It forgives everything except genius.",
        "An idea that is not dangerous is unworthy of being called an idea at all.",
        "I can stand brute force, but brute reason is quite unbearable.",
        "There is something terribly morbid in the modern sympathy with pain.",
        "The only thing to do with good advice is to pass it on. It is never of any use to oneself.",
        "A man who does not think for himself does not think at all.",
        "The advantage of the emotions is that they lead us astray.",
        "The only difference between the saint and the sinner is that every saint has a past, and every sinner has a future.",
        "I am the only person in the world I should like to know thoroughly.",
        "The books that the world calls immoral are books that show the world its own shame.",
        "A dreamer is one who can only find his way by moonlight, and his punishment is that he sees the dawn before the rest of the world.",
        "The well-bred contradict other people. The wise contradict themselves.",
        "I have made an important discovery... that alcohol, taken in sufficient quantities, produces all the effects of intoxication.",
        "I don't want to be at the mercy of my emotions. I want to use them, to enjoy them, and to dominate them.",
        "The moment you think you understand a great work of art, it's dead for you."
    )
    
    /**
     * Returns a random quote from the collection.
     * Each call returns a different quote, providing variety on each app load.
     * 
     * @return A random quote from the quotes collection
     */
    fun getRandomQuote(): String {
        return quotes.random()
    }
}
