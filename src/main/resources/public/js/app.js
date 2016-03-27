var Model = {
    /**
     * Return keywords that match the given partial free text query
     **/
    getKeywords: function(query) {
        var keywords = Model.getAllKeywords().filter(function(keyword) { return keyword.toLowerCase().indexOf(query.toLowerCase()) === 0});
        keywords.sort();
        return keywords;
    },

    /**
     * Return all the possible keyword phrases
     **/
    getAllKeywords: function() {
        var keywordsIndex = [];
        podcasts.forEach(function(podcast) {
            podcast.analysis.entities.forEach(function(entity) {
                keywordsIndex[entity.text] = 1;
            });
            podcast.analysis.concepts.forEach(function(concept) {
                keywordsIndex[concept.text]=1;
            });
            podcast.analysis.keywords.forEach(function(keyword) {
                keywordsIndex[keyword.text] = 1;
            });
        });
        return Object.keys(keywordsIndex);
    },

    /**
     * Return podcasts that match the given keyword search query
     **/
    getFilteredResults: function(searchQuery) {
        if (!searchQuery || searchQuery === '') return podcasts;
        return podcasts.filter(function(podcast) {
            podcast.analysis.relations = undefined;
            return JSON.stringify(podcast.analysis).toLowerCase().indexOf(searchQuery.toLowerCase()) >= 0;
        });
    },

    /**
     * Return a specific podcast by id
     **/
    getById: function(id) {
        for (i in podcasts)
            if (Number(podcasts[i].id) === id) return podcasts[i];
    }
}

var View = {
    /**
     * Render given results list based on the given podcast and keyword search query
     **/
    renderResults: function(podcasts, keywords) {
        $('#results-count').html(podcasts.length);

        var resultTemplate = _.template($('#template-result').html(), undefined, { variable: 'data' });

        var resultsDom = "";
        podcasts.forEach(function(podcast) {
            resultsDom += resultTemplate({podcast: podcast, keywords: (keywords == '') ? undefined : keywords});
        });

        $('#results').html(resultsDom);
    },

    /**
     * Render transcript for the given podcast in a new window
     **/
    renderTranscript: function(podcast) {
        window.open().document.write("<h1>" + podcast.title + "</h1>" + podcast.text);
    }
}

var Controller = {
    /**
     * Respond to search auto-completion queries
     **/
    getKeywords: function(searchQuery, callback) {
        var keywords = Model.getKeywords(searchQuery);
        callback(keywords);
    },

    /**
     * Update search results based on the given query
     **/
    updateResults: function(searchQuery) {
        var podcasts = Model.getFilteredResults(searchQuery);
        View.renderResults(podcasts, searchQuery);
    },

    /**
     * Show the selected podcast transcript
     **/
    showTranscript: function(id) {
        var podcast = Model.getById(id);
        if (podcast)
            View.renderTranscript(podcast);
    }
}
