# TASK 1

## Opis

Tworzy przepływ obrazu Docker z sprawdzeniem jakości. Używa narzędź hadolint, trivy oraz dockle oraz OWASP dependency check. Składa się z 4 kroków, każdy z których jest w stanie przerwać przepływ przy wykryciu błędów. Kolejnym zadaniem w obrębach tego projektu stażowego, było użycie utworzonego przepływu do stworzenia CI pipeline'u. Ostatecznie zostało zaprojektowane dwa pipeline'y.<br>

Użycie:
```
export GHCR_TOKEN=<token>
export NVD_API_KEY=[api_key]
make release SERVICE=<backend|frontend> [TAG=<tag>]
```

Użycie CI:
```
Dowolny push na repo uruchamia główny pipeline.

Cron pipeline się uruchamia codziennie o 1 godzinie UTC, albo ręcznie z panelu GitHub.
```

- `SERVICE` - obraz serwisu do zbudowania, sprawdzenia i wypchnięcia. Domyślnie jest ustawiony na `backend`
- `TAG` - tag obrazu, domyślnie jest w formie git short SHA
- `GHCR_TOKEN` - token do rejestru udostępniony przez powłokę. Zbędne przy użyciu w prawdziwym `CI`, ponieważ byłby dostępny przez manager sekretów np. `GitHub Secrets`.
- `NVD_API_KEY` - klucz api generowany przez NIST (National Institute of Standarts and Technology). Nie jest niezbędny, ale bez niego pobieranie bazy danych NVD do OWASP Dependency Check, może się drastycznie wydłużyć. Jest darmowy do założenia.

**Przepływ głównego pipeline'u CI:**
1. repo-checks
   - Checkout.
   - Tworzy cache m2, potrzebny dla narzędzi skanowania, przy targetcie będącym w Maven.
   - Załadowuje cache `m2`.
   - Uruchamia narzędzie Hadolint, skanuje Dockerfile'y obu obrazów.
3. nvd-update - służy do sprawdzenia stanu cache `nvd-v4`.
   - Checkout.
   - Pobiera cache używając klucza.
   - Seeduję go danymi, jeśli jest pusty seduje go pobraną wcześniej biblioteką NVD z prywatnego GitHub Release.
5. pipeline - po wykonaniu kroku 1. oraz 2. uruchamia główny przebieg. Równolegle wykonuje się dla obu obrazów - `backend` oraz `frontend`.
   - Checkout.
   - Odzyskuje repozytorium .m2 z cache `m2`.
   - Odzyskuje bazę danych NVD z cache `nvd-v4`.
   - Tworzy cache do przechowywania `Trivy`.
   - Buduje obraz.
   - Wykonuje skan na obrazie `Dockle` + `Trivy`.
   - Generuje `SBOM` (przy użyciu `Trivy` w formacie `CycloneDX`) i upload'uje go jako artefakt.
   - Wykonuje OWASP Dependency Check, tylko dla obrazu `backend`. Następnie upload'uje go jako kolejny artefakt.
   - Wypycha sprawdzony obraz do GHCR

**Przepływ cron do update'owania bazy danych NVD:**
1. keepalive - oryginalnie zamysł polegał na tym, żeby uniknąć usunięcia cache'u z bazą NVD poprzez kroniczny job, który by używał cache raz na kilka dni. Ostatecznie zostało przerobione na kron job, który mimo zachowania cache przy życiu, jeszcze wykonuje update samej bazy danych. Taka potrzeba wynika z tego powodu, że NVD jest aktualizowane codziennie.
   - Checkout.
   - Odzyskanie cache `nvd-v4`.
   - Update bazy danych NVD, używa zrobiony wcześniej klucz API z Secrets.
   - Zapis zaktualizowanej bazy danych do cache `nvd-v4`.

### Dokładny opis kroków

#### **1. Skanuje Dockerfile za pomocą narzędzia hadolint:**
```
docker run --rm -i hadolint/hadolint < $(DOCKERFILE)
```
Uruchamia tymczasowy kontener z obrazem hadolint i pcha zmienną `DOCKERFILE` zawierającą ścieżkę do Dockerfile. Przy wykryciu błędu automatycznie zwraca błąd przerywając przepływ.
#### **2. Buduje obraz:**
```
docker build \
		-t $(IMAGE_NAME):latest \
		-t $(IMAGE_NAME):$(TAG) \
		./$(SERVICE)
```
Buduje obraz podanego przez użytkownika `SERVICE` z dwoma tagami:
- `latest` - domyślny tag ostatniego wypchnięcia obrazu.
- `TAG` - domyślnie git short SHA, może być ustawiony przez użytkownika.
#### **3. Sprawdza przy pomocy narzędź trivy oraz dockle jakość obrazów:**
Ten etap składa się z dwóch kroków.
##### **Trivy**
```
docker run \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v trivy-cache:/root/.cache \
		--rm aquasec/trivy \
		image $(IMAGE_NAME):$(TAG) \
		--severity HIGH,CRITICAL \
		--exit-code 1
```
Uruchamia tymczasowy kontener z obrazem trivy. Posiada dwa mounty wolumenów, dwa argumenty wywoływania trivy'ego. Przerywa przepływ przy znalezieniu podatności ocenianych jako `HIGH` oraz `CRITICAL`. W pozostałych przypadkach pozostawia informacje o mniejszych błędach . 

1. Wolumeny:
   - `trivy-cache` - cache triviego, wolumen wymagany od twórców.
   - `docker.sock` - bez tego wolumenu Trivy nie jest w stanie zobaczyć obrazu, który chcemy przeskanować. Daje Trivy'emu dostęp do Docker daemon. 
2. Argumenty wywoływania:
   - `--severity` - przerywa przepływ przy wykryciu podatności o "poważności" `HIGH` oraz `CRITICAL`
   - `--exit-code` - domyślnie trivy przy wykryciu błędów zwraca kod `0`, pełniać bardziej funkcję informacyjną. Wymuszenie zwracania kodu `1` przy wykryciach błędów automatycznie przerywa proces w `Makefile`.

##### **Dockle**
```
docker run --rm \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(shell pwd)/.dockleignore:/.dockleignore \
		goodwithtech/dockle:v$(DOCKLE_VERSION) \
		$(IMAGE_NAME):$(TAG) --exit-code 1 --exit-level fatal 
```
Uruchamia tymczasowy kontener z obrazem Dockle. Używa dwóch wolumenów oraz dwóch argumentów. Skanuje obraz na podatności, przy wykryciu błędów o progu minimalnym `fatal` przerywa działanie przepływu.
1. Wolumeny:
   - `.dockleignore` - plik z konfiguracją ignorowanych podatności. Zawiera jedną podatność wynikającą z obrazu nginx używanego w `frontend`.
   - `docker.sock` - bez tego wolumenu Dockle nie jest w stanie zobaczyć obrazu, który chcemy przeskanować. Daje Dockle'u dostęp do Docker daemon. 
2. Argumenty wywoływania:
   - `--exit-level` - przerywa przepływ przy wykryciu podatności o progu minimalnym `fatal`.
   - `--exit-code` - domyślnie trivy przy wykryciu błędów zwraca kod `0`, pełniać bardziej funkcję informacyjną. Wymuszenie zwracania kodu `1` przy wykryciach błędów automatycznie przerywa proces w `Makefile`.

#### **4. Wypycha obraz do rejestru GHCR:**
```
@test -n "$(GHCR_TOKEN)" || (echo "Error: GHCR_TOKEN not set" && exit 1)
@echo "$(GHCR_TOKEN)" | docker login ghcr.io -u $(GHCR_USER) --password-stdin
	
docker push $(IMAGE_NAME):$(TAG)
docker push $(IMAGE_NAME):latest
```
Sprawdza czy token do GHCR został podany przez użytkonika, następnie wykonuje login do ghcr.io. Jak to przejdzie wypycha obraz o tagu `TAG` oraz `latest` do rejestru.

## Demostracja dziłania

Do sprawdzenia działania używałem kodu źródłowego z mojego projektu [Trippy](https://github.com/MarshallBjorn/trippy), przeniosłem dwa serwisy:
- `backend` - napisany w Java i Springboot RESTapi.
- `frontend` - napisany w Vue, ustawiony pod produkcję czyli serwowanie plików przez `nginx` a nie `Vite`.

Obraz `backend` ma problemy wykryte przez Trivy oraz Dockle. Natomiast `frontend` jest czysty pod wzgłędem Trivy'ego, oraz posiadał jeden błąd false-positive. Więc `backend` nie przechodzi, a `frontend` przechodzi. Logi do przepływu obu:
- [task-1-backend-log.txt](./public_logs/task-1-backend-log.txt)
- [task-1-frontend-log.txt](./public_logs/task-1-frontend-log.txt)

## Czego się nauczyłem
Jest to sekcja gdzie wspominam jakieś nieoczywiste rzeczy które poznałem przy robieniu zadania, albo coś co mnie zaskoczyło po prostu.
- Formatowanie Makefile - poznałem że Makefile jest dość wredny pod wzgłędem formatowania. W przypadku wywoływań poleceń wieloliniowych, rozbitych `\` prosta spacja potrafi złamać cały przepływ.
- Używanie tymczasowych kontenerów z narzędziami. Nie byłem świadomy takich możliwości, pozwala na używanie narzędzi, bez konieczności ich instalacji na maszynie.
- W prawdziwym `CI`, pobieranie `GHCR_TOKEN`'u jako zmiennej wewnątrz Makefile jest zbędne. CHYBA ŻE obraz jest publiczny to ogólnie część z logowaniem do rejestru można pominąć.
- Narzędzia do skanowania obrazów czasami zwracają false-positives. W tym zadaniu konfiguracja tych narzędź jest bardzo prymitywna, mam tego świadomość.
- `-v $(shell pwd)/.dockleignore:/.dockleignore` może niekoniecznie działać w CI. Ponieważ `pwd` może nie być tam gdzie tego faktycznie chcemy.
- Nauczyłem się różnicy w Makefile'owych operatorach `?=`, `:=` oraz `=`. 
- Obecny Makefile pobiera ostatnią dostępną wersję Dockle'a. Do przypadków gdyby miało to zawieźć ustawiona została wersja fallback'owa. Co nadal nie oznacza że jest to w pełni dobre rozwiązanie. Starsza wersja Dockle'a może mieć potencjalne bugi.
- Do prawdziwego `CI`, znalazłym sposób wymuszać jawnie `exit-code` 1 na narzędziu `hadolint`. Obecnie sprawdziłem że faktycznie przerywa cały pipeline przy problemach, ale mimo wszystko lepiej stosować tu zasadę "ufaj, ale sprawdzaj". Jakby miało się coś zmienić w działaniu narzędzia, to przepływ tego nie wyłapie.
