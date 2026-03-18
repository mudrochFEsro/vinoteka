-- Wine Categories
INSERT INTO categories (name, slug) VALUES
('Cervene vina', 'cervene-vina'),
('Biele vina', 'biele-vina'),
('Ruzove vina', 'ruzove-vina'),
('Sumive vina', 'sumive-vina'),
('Dezertne vina', 'dezertne-vina');

-- Red Wines (Cervene vina) - category_id = 1
INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Frankovka modra 2021', 'Suche cervene vino s tonmi cervenych bobul a korenia. Elegantne taniny a dlha dochut. Vhodne k cervenemu masu a syrom.', 12.90, 50, 1, CURRENT_TIMESTAMP),
('Cabernet Sauvignon 2020', 'Plne cervene vino s aromou cernych ribezli, cedra a vanilky. Zrelo 12 mesiacov v dubovych sudoch apply. Idealne k steaku.', 18.50, 35, 1, CURRENT_TIMESTAMP),
('Alibernet 2021', 'Intenzivne cervene vino s hlbokou farbou. Chut plna zrelych sliviek a cokolady. Vyborne k duseneemu masu.', 14.90, 40, 1, CURRENT_TIMESTAMP),
('Merlot 2020', 'Jemne a zamatove cervene vino s tonmi visni a sliviek. Prijemna kyselinka a dlha ovocna dochut.', 15.90, 45, 1, CURRENT_TIMESTAMP),
('Svatovavrinecke 2021', 'Tradicne slovenske cervene vino. Ovocna vona s tonmi cernych ceresnii. Stredne telo a jemne taniny.', 11.50, 60, 1, CURRENT_TIMESTAMP),
('Dunaj 2020', 'Autochtonna slovenska odroda. Tmavocervena farba, aroma lesneho ovocia a korenia. Skvele k divine.', 16.90, 30, 1, CURRENT_TIMESTAMP);

-- White Wines (Biele vina) - category_id = 2
INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Rizling vlassky 2022', 'Suche biele vino s kvetinovou vonou a citrusovymi tonmi. Osviezujuca kyselinka, idealne ako aperitiv.', 9.90, 70, 2, CURRENT_TIMESTAMP),
('Veltlinske zelene 2022', 'Sviezie biele vino s tonmi zelenych jablk a bielych kvetov. Mineralne a elegantne.', 10.50, 65, 2, CURRENT_TIMESTAMP),
('Chardonnay 2021', 'Plne biele vino zrele v dubovych sudoch. Aroma tropickeho ovocia, vanilky a masla. Vhodne k hydine.', 14.90, 40, 2, CURRENT_TIMESTAMP),
('Sauvignon Blanc 2022', 'Aromaticke biele vino s intenzivnou vonou bazalky, egresia a tropickeho ovocia. Sviezia kyselinka.', 12.90, 55, 2, CURRENT_TIMESTAMP),
('Muller Thurgau 2022', 'Polosuche biele vino s jemnou sladkostou. Tony muškatu a kvetinovych tonov. Prijemne osviezujuce.', 8.90, 80, 2, CURRENT_TIMESTAMP),
('Devin 2022', 'Aromaticka slovenska odroda. Intenzivna vona muskatoveho hrozna a ruzi. Polosuche a velmi elegantne.', 13.50, 45, 2, CURRENT_TIMESTAMP),
('Tramín cerveny 2021', 'Vino s nezamenitelnou vonou ruzi a litchi. Plna a zamatova chut. Perfektne k asijskej kuchyni.', 15.90, 35, 2, CURRENT_TIMESTAMP);

-- Rose Wines (Ruzove vina) - category_id = 3
INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Cabernet Rose 2022', 'Suche ruzove vino s jahodovou vonou a osviezujucou kyselinkou. Idealne na letne dni.', 10.90, 50, 3, CURRENT_TIMESTAMP),
('Frankovka Rose 2022', 'Sviezie rose s tonmi malín a cervenych ribezli. Suche a elegantne. Skvele k lahkym jedlam.', 9.90, 55, 3, CURRENT_TIMESTAMP),
('Merlot Rose 2022', 'Jemne ruzove vino s ovocnymi tonmi a kvetinovymi aromami. Polosuche a velmi pitne.', 11.50, 45, 3, CURRENT_TIMESTAMP);

-- Sparkling Wines (Sumive vina) - category_id = 4
INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Sekt Brut', 'Kvalitny slovensky sekt vyrobeny tradicnou metodou. Jemne perlenie, tony citrusov a brioche.', 14.90, 40, 4, CURRENT_TIMESTAMP),
('Sekt Demi-Sec', 'Polosuchy sekt s jemnou sladkostou. Aroma zreleho ovocia a medovych tonov. Perfektny na oslavy.', 13.90, 45, 4, CURRENT_TIMESTAMP),
('Sekt Rose Brut', 'Elegantny ruzovy sekt s jemnym perlenim. Tony malín a jahodniky. Osviezujuci a slavnostny.', 15.90, 35, 4, CURRENT_TIMESTAMP),
('Prosecco DOC', 'Talianske sumive vino s charakteristickou ovocnou vonou. Lahke a osviezujuce.', 12.90, 60, 4, CURRENT_TIMESTAMP);

-- Dessert Wines (Dezertne vina) - category_id = 5
INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Tokajsky vyber 5-putňovy', 'Legendarne sladke vino z Tokajskej oblasti. Koncentrovana chut sušeneho ovocia, medu a korenia.', 45.00, 20, 5, CURRENT_TIMESTAMP),
('Cibeba', 'Sladke vino z prezretých bobúľ. Intenzivna sladkost vyvazena kyselinkou. Vhodne k dezertom.', 22.90, 25, 5, CURRENT_TIMESTAMP),
('Ľadove vino 2021', 'Raritne vino z hrozna zozbieraneho v mraze. Koncentrovana sladkost a dlha exoticka dochut.', 38.00, 15, 5, CURRENT_TIMESTAMP);
