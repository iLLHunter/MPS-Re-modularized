import org.ajoberstar.grgit.gradle.GrgitService
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*

import javax.inject.Inject

abstract class OutputChangelog extends DefaultTask {

    private final Provider<GrgitService> grgitService

    @Input
    final Provider<String> releaseType
    @Input
    final Provider<String> modVersion
    @Input
    @Optional
    final Provider<String> currentCommit
    @Input
    @Optional
    final Provider<String> previousCommit
    @Optional
    @InputFile
    final RegularFileProperty releaseNotes
    @OutputFile
    final RegularFileProperty outputFile

    @Inject
    OutputChangelog(Provider<GrgitService> grgitService) {
        this.grgitService = grgitService
        usesService(grgitService)
        releaseType = providerFactory.gradleProperty('release_type')
        modVersion = providerFactory.gradleProperty('mod_version')
        currentCommit = providerFactory.environmentVariable('GIT_COMMIT')
        previousCommit = providerFactory.environmentVariable('GIT_PREVIOUS_SUCCESSFUL_COMMIT')
                .orElse(providerFactory.environmentVariable('GIT_PREVIOUS_COMMIT'))
        releaseNotes = objectFactory.fileProperty()
        outputFile = objectFactory.fileProperty().convention(projectLayout.buildDirectory.file('changelog.md'))
    }

    @Inject
    protected abstract ProviderFactory getProviderFactory()

    @Inject
    protected abstract ObjectFactory getObjectFactory()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

    @TaskAction
    void execute() {
        def generatedChangelog = ''

        if (currentCommit.isPresent() && previousCommit.isPresent()) {
            generatedChangelog = ''
            //Use the service to avoid eagerly instantiating the grgit instance, and only do so when we actually need it
            // for usage in generating the changelog for either CF or Modrinth
            def commits = grgitService.get().grgit.log {
                range(previousCommit.get(), currentCommit.get())
            }.reverse()
            for (def commit : commits) {
                //Use full message rather than short message to get any new lines, and trim it so that any trailing new lines
                // get removed so that we don't end up with extra spaces
                String message = commit.fullMessage.trim()
                if (!message.startsWith('Merge branch') && !message.startsWith('Merge pull request') && !message.contains('[no-changelog]')) {
                    //Ignore Merges and PR Merges
                    message = message.replaceAll('#(\\d+)', { match ->//turn issues/prs into links (github currently supports prs being linked as issues)
                        return "[${match[0]}](https://github.com/iLLHunter/MPS-Re-modularized/issues/${match[1]})"
                    }).replaceAll('\\n', '  \n\t')//convert new lines that are part of a commit message into new lines and a tab
                    if (generatedChangelog != '') {
                        //If this isn't the first commit prepend an extra newline
                        generatedChangelog += '  \n'
                    }
                    generatedChangelog += "[${commit.getAbbreviatedId()}](https://github.com/iLLHunter/MPS-Re-modularized/commit/${commit.id}) - ${message}"
                }
            }
            logger.lifecycle('Changelog generated')
        }

        if (releaseNotes.isPresent()) {
            //Add any version specific changelog stuff
            generatedChangelog = releaseNotes.get().asFile.text + (generatedChangelog ? '\n\n' + generatedChangelog : '')
        } else {
            // Public source exports omit the private release notes.
            generatedChangelog = "MPS Re-modularized ${modVersion.get()}\n" + (generatedChangelog ? '\n' + generatedChangelog : '')
        }

        if (releaseType.get() == 'alpha') {
            //Add a warning at the top about what an alpha build means
            generatedChangelog = 'MPS Re-modularized alpha build. See the release notes for its validation status.\n\n' + generatedChangelog
        }

        outputFile.get().asFile.text = generatedChangelog
    }
}
