# Contributing to Cyber Search 
Thank you for considering a contribution to Cyber Search! This guide explains how to:
* Get started
* Development workflow
* Get help if you encounter trouble


## Get in touch
Before starting to work on a feature or a fix, please open an issue to discuss the use case or bug with us. This can 
save both you and us a lot of time. For any non-trivial change, we'll ask you to create a short design document explaining:

* Why is this change done? What's the use case?
* What will the API look like? (For new features)
* What test cases should it have? What could go wrong?
* How will it roughly be implemented? (We'll happily provide code pointers to save you time)


## Development Workflow

### Development Setup
Please, use [development environment setup guide](./dev-environment.md).

### Make Changes
Use this [Architecture Overview](http://docs.cybernode.io/cybernode/components/search/) as a start point for making changes.

### Local Check

Several checks should passed to succeed build.
* [Detekt](https://github.com/arturbosch/detekt) code analyze tool should not report any issues
* [JUnit](https://junit.org/junit5/) tests should pass

Before committing you changes, please, run local project check by:
```bash
./gradlew build    //linux, mac
gradlew.bat build  //windows
``` 

### Creating Commits And Writing Commit Messages
The commit messages that accompany your code changes are an important piece of documentation, please follow these guidelines when writing commit messages:
 solidity/CONTRIBUTING.md

* Keep commits discrete: avoid including multiple unrelated changes in a single commit
* Keep commits self-contained: avoid spreading a single change across multiple commits. A single commit should make sense in isolation
* Add GitHub issue to [CHANGELOG.md](/CHANGELOG.md)
* Include GitHub issue in the commit message on a first line at the beginning. Example:
```
#123 Refactor CONTRIBUTING.md

--Add Creating Commits And Writing Commit Messages Section
--Another Section
```

### Submitting Your Change
After you submit your pull request, a core developer will review it. It is normal that this takes several 
iterations, so don't get discouraged by change requests. They ensure the high quality that we all enjoy.


## Getting Help
If you run into any trouble, please reach out to us on the issue you are working on.


## Our Thanks
We deeply appreciate your effort toward improving Search. For any contribution, large or small, you will be immortalized
 in the release notes for the version you've contributed to.
