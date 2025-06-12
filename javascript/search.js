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
        const p = document.createElement("p");
        p.textContent = "No results found"
        resultsDiv.appendChild(p);
        return;
    }

    const ul = document.createElement("ul");
    ul.className = "tna-ul"
    for (const item of results) {
        const li = document.createElement("li");
        const link = document.createElement('a');
        link.href = item.url;
        const strong = document.createElement('strong');
        strong.textContent = item.title;
        link.appendChild(strong);
        const br = document.createElement('br');
        const small = document.createElement('small');
        small.textContent = item.content.slice(0, 150) + '...';
        li.appendChild(link);
        li.appendChild(br);
        li.appendChild(small);
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