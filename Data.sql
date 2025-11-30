-- Drop tables if exist (in dependency order)
IF OBJECT_ID('PencatatanAir', 'U') IS NOT NULL DROP TABLE PencatatanAir;
IF OBJECT_ID('PerangkatIOT', 'U') IS NOT NULL DROP TABLE PerangkatIOT;
IF OBJECT_ID('Kepemilikan', 'U') IS NOT NULL DROP TABLE Kepemilikan;
IF OBJECT_ID('UnitSarusun', 'U') IS NOT NULL DROP TABLE UnitSarusun;
IF OBJECT_ID('Lantai', 'U') IS NOT NULL DROP TABLE Lantai;
IF OBJECT_ID('Tower', 'U') IS NOT NULL DROP TABLE Tower;
IF OBJECT_ID('Rusunami', 'U') IS NOT NULL DROP TABLE Rusunami;
IF OBJECT_ID('LogPengguna', 'U') IS NOT NULL DROP TABLE LogPengguna;
IF OBJECT_ID('Pemilik', 'U') IS NOT NULL DROP TABLE Pemilik;


-- Table: Pemilik
CREATE TABLE Pemilik (
    NIK VARCHAR(20) PRIMARY KEY,
    nama VARCHAR(100),
    alamat VARCHAR(255),
    noPonsel VARCHAR(15),
    peran VARCHAR(50),
	isActive VARCHAR(5)
);

INSERT INTO Pemilik VALUES 
('3275010101010001', 'Agus Santoso', 'Jl. Anggrek 5', '08112008203', 'Pemilik', 'yes'),
('3275010101010002', 'Ahmad Santiago', 'Jl. Garuda 24', '087784769904', 'Administrator', 'yes'),
('3275010101010003', 'Dewi Lestari', 'Jl. Mawar 2', '081221894623', 'Pengelola', 'yes');

-- Table: Tower
CREATE TABLE Tower (
	idTower INT PRIMARY KEY IDENTITY(1,1),
	nama VARCHAR(50)
)

INSERT INTO Tower (nama) VALUES
('Tower A'),
('Tower B'),
('Tower C');

-- Table: UnitSarusun
CREATE TABLE UnitSarusun (
    idUnit VARCHAR(10) PRIMARY KEY,
	status VARCHAR(10),
	NIK VARCHAR(20),
	idLantai INT,
	isActive VARCHAR(5),
	FOREIGN KEY (NIK) REFERENCES Pemilik(NIK)
);

INSERT INTO UnitSarusun (idUnit, status, NIK,idLantai, isActive) VALUES 
('A0101', 'Occupied', '3275010101010001', 1, 'yes'), 
('A0201', 'Available', null, 2, 'yes'), 
('A0301', 'Occupied', '3275010101010003', 3, 'yes');

-- Table: Kepemilikan
CREATE TABLE Kepemilikan (
    idKepemilikan INT PRIMARY KEY IDENTITY(1,1),
    NIK VARCHAR(20),
    idUnit VARCHAR(10),
    FOREIGN KEY (NIK) REFERENCES Pemilik(NIK),
    FOREIGN KEY (idUnit) REFERENCES UnitSarusun(idUnit)
);

INSERT INTO Kepemilikan (NIK, idUnit) VALUES 
('3275010101010001', 'A0101')

-- Table: PerangkatIOT
CREATE TABLE PerangkatIOT (
    SN VARCHAR(30) PRIMARY KEY,
    status VARCHAR(20),
    idUnit VARCHAR(10),
	isActive VARCHAR(5),
	NIK VARCHAR(20),
    FOREIGN KEY (idUnit) REFERENCES UnitSarusun(idUnit),
	FOREIGN KEY (NIK) REFERENCES Pemilik(NIK)
);

INSERT INTO PerangkatIOT (SN, status, idUnit, isActive, NIK) VALUES 
('SN001', 'Aktif', 'A0101', 'yes', '3275010101010001'),
('SN004', 'Nonaktif', 'A0101', 'yes', '3275010101010001'),
('SN002', 'Nonaktif', 'A0201', 'yes', '3275010101010001'),
('SN003', 'Aktif', 'A0301', 'yes', '3275010101010001');

--Pencatatan Air
CREATE TABLE PencatatanAir (
    idPencatatan INT PRIMARY KEY IDENTITY(1,1),   -- Primary Key untuk pencatatan air
    jumlahPemakaian INT,            -- Jumlah pemakaian air (dalam liter)
    waktu TIME,                     -- Waktu pencatatan
    tanggal DATE,                   -- Tanggal pencatatan
    aksi VARCHAR(50),               -- Aksi yang dilakukan, seperti "Buka", "Tutup"
	NIK VARCHAR(20),
	SN VARCHAR(30),
	FOREIGN KEY (NIK) REFERENCES Pemilik(NIK),
	FOREIGN KEY (SN) REFERENCES PerangkatIoT(SN)
);

INSERT INTO PencatatanAir (jumlahPemakaian, waktu, tanggal, aksi, NIK, SN)
VALUES
(50, '10:00:00', '2025-05-01', 'Buka aliran air', '3275010101010001', 'SN001'),
(45, '11:00:00', '2025-05-01', 'Tutup aliran air', '3275010101010001', 'SN001'),
(60, '12:00:00', '2025-05-01', 'Buka aliran air', '3275010101010001', 'SN001'),
(30, '08:00:00', '2025-05-02', 'Tutup aliran air', '3275010101010001', 'SN001'),
(40, '09:00:00', '2025-05-02', 'Buka aliran air', '3275010101010001', 'SN001'),
(35, '15:00:00', '2025-05-03', 'Tutup aliran air', '3275010101010001', 'SN001');

CREATE TABLE LogPengguna(
    idLog INT PRIMARY KEY IDENTITY(1,1),
    aktivitas VARCHAR(50),
    tanggal DATE,
    waktu TIME,
    NIK VARCHAR(20),
    FOREIGN KEY (NIK) REFERENCES Pemilik(NIK),
);

INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES
('Login', '2025-05-26', '08:00:00', '3275010101010001'),
('Logout', '2025-05-26', '09:00:00', '3275010101010002'),
('Akses Data', '2025-05-26', '10:15:00', '3275010101010003'),
('Login', '2025-05-26', '11:00:00', '3275010101010001'),
('Logout', '2025-05-26', '14:00:00', '3275010101010002'),
('Akses Data', '2025-05-26', '16:30:00', '3275010101010003');


select * from Kepemilikan
select * from Pemilik
select * from PencatatanAir
select * from PerangkatIOT
select * from UnitSarusun

select * from PerangkatIOT
select * from UnitSarusun

select noPonsel, peran from Pemilik
select * from UnitSarusun
SELECT * FROM LogPengguna

SELECT pi.SN, pi.status FROM PerangkatIOT pi 
JOIN UnitSarusun ua ON pi.idUnit = ua.idUnit 
WHERE ua.idUnit = 'A0101' AND pi.isActive = 'yes'