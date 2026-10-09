const CACHE='kaito-zangi-test07';
self.addEventListener('install',e=>{self.skipWaiting();e.waitUntil(caches.open(CACHE).then(c=>c.addAll(['./','./icon-192.png?v=7','./icon-512.png?v=7','./manifest.webmanifest'])));});
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k))))));
self.addEventListener('fetch',e=>{if(e.request.method!=='GET'||new URL(e.request.url).origin!==self.location.origin||e.request.url.endsWith('.apk'))return;e.respondWith(fetch(e.request).catch(()=>caches.match(e.request)));});
