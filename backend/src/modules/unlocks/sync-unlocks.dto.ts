import { Type } from 'class-transformer';
import {
  ArrayMaxSize,
  IsArray,
  IsBoolean,
  IsInt,
  IsISO8601,
  IsOptional,
  IsString,
  IsUUID,
  Min,
  ValidateNested,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class DailySummaryDto {
  @ApiProperty({ example: '2026-05-25' })
  @IsString()
  date!: string;

  @ApiProperty({ example: 87 })
  @IsInt()
  @Min(0)
  unlockCount!: number;

  @ApiPropertyOptional({ example: 0, description: 'Amount in paise' })
  @IsInt()
  @Min(0)
  @IsOptional()
  payableAmountP?: number;

  @ApiPropertyOptional({ example: false })
  @IsBoolean()
  @IsOptional()
  capped?: boolean;
}

export class UnlockEventDto {
  @ApiProperty({ format: 'uuid' })
  @IsUUID()
  id!: string;

  @ApiProperty({ example: '2026-05-25T10:22:31.000Z' })
  @IsISO8601()
  ts!: string;

  @ApiProperty({ example: 'usage_stats' })
  @IsString()
  source!: string;
}

export class SyncUnlocksDto {
  @ApiProperty({ type: [DailySummaryDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => DailySummaryDto)
  @ArrayMaxSize(31)
  dailySummaries!: DailySummaryDto[];

  @ApiPropertyOptional({ type: [UnlockEventDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UnlockEventDto)
  @ArrayMaxSize(500)
  @IsOptional()
  events?: UnlockEventDto[];
}

export class SyncUnlocksResponseDto {
  @ApiProperty()
  synced!: number;

  @ApiProperty()
  events!: number;
}

export class DailySummaryResponseDto {
  @ApiProperty()
  date!: string;

  @ApiProperty()
  unlockCount!: number;

  @ApiProperty()
  payableAmountP!: number;

  @ApiProperty()
  capped!: boolean;

  @ApiProperty()
  computedAt!: string;
}
