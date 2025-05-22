function getQueryParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name) || "";
}

function renderResults(query) {
    const resultsDiv = document.getElementById("results");
    const queryLower = query.toLowerCase();

    const results = searchIndex.filter(item =>
        item.title.toLowerCase().includes(queryLower) ||
        item.content.toLowerCase().includes(queryLower)
    );

    if (results.length === 0) {
        resultsDiv.innerHTML = "<p>No results found.</p>";
        return;
    }

    const ul = document.createElement("ul");
    ul.className = "tna-ul"
    for (const item of results) {
        const li = document.createElement("li");
        li.innerHTML = `<a href="${item.url}"><strong>${item.title}</strong></a><br><small>${item.content.slice(0, 150)}...</small>`;
        ul.appendChild(li);
    }

    resultsDiv.innerHTML = "";
    resultsDiv.appendChild(ul);
}

// Load query and perform search on page load
window.onload = () => {
    const query = getQueryParam("q");
    if (query) {
        document.getElementById("search").value = query;
        renderResults(query);
    }
}