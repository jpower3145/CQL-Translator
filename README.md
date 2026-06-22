LancsBox Natural Language to CQL Translator

A functional Java desktop prototype that translates English search queries into valid Corpus Query Language (CQL) expressions supported by #LancsBox.

This tool integrates a local LLM via Ollama learning from the #LancsBox cheat sheet syntax.

Prerequisites

To run this application locally, you must have the following installed on your machine:

Ollama (Local LLM engine)

Setup & Running the App

1. Start your local LLM

Ensure Ollama is running and hosting the model in the background:

ollama run phi3

2. Clone and Run the Java App

In your terminal, navigate to the root directory of this project (LLM) and run the build script:

./gradlew run
